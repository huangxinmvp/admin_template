package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.config.BaseConfig;
import com.hiking.treasure.domain.vo.FileCenterMetricVO;
import com.hiking.treasure.domain.vo.FileCenterStatsVO;
import com.hiking.treasure.entity.File;
import com.hiking.treasure.mapper.FileMapper;
import com.hiking.treasure.service.FileService;
import com.hiking.treasure.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 文件存储记录 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    private final BaseConfig baseConfig;
    private final SystemConfigService systemConfigService;

    @Override
    public File upload(MultipartFile multipartFile, String bizType) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        Integer maxSizeMb = systemConfigService.getInt("file.maxSizeMb", 20);
        long maxBytes = (long) Objects.requireNonNullElse(maxSizeMb, 20) * 1024 * 1024;
        if (multipartFile.getSize() > maxBytes) {
            throw new BusinessException(400, "文件大小不能超过 " + maxSizeMb + " MB");
        }
        try {
            byte[] bytes = multipartFile.getBytes();
            String tenantId = Objects.requireNonNullElse(SecurityUtils.getTenantId(), "public");
            String originalFilename = StringUtils.hasText(multipartFile.getOriginalFilename()) ? multipartFile.getOriginalFilename() : "file.bin";
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String normalizedExtension = extension == null ? "" : "." + extension.toLowerCase();
            String allowedTypes = systemConfigService.getString("file.allowedTypes", "");
            if (StringUtils.hasText(allowedTypes)) {
                boolean matched = Arrays.stream(allowedTypes.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .map(String::toLowerCase)
                        .anyMatch(normalizedExtension::equals);
                if (!matched) {
                    throw new BusinessException(400, "当前文件类型不在允许上传范围内");
                }
            }
            String objectName = tenantId + "/" + LocalDate.now() + "/" + UUID.randomUUID() + (extension == null ? "" : "." + extension);
            Path root = Paths.get(baseConfig.getPath().getUpload()).toAbsolutePath().normalize();
            Path target = root.resolve(objectName).normalize();
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);

            File file = new File();
            file.setFileName(originalFilename);
            file.setObjectName(objectName);
            file.setUrl("/api/file/download/" + objectName.replace("/", "__"));
            file.setContentType(multipartFile.getContentType());
            file.setFileSize((long) bytes.length);
            file.setStorageType("local");
            file.setStorageProvider(systemConfigService.getString("file.storageProvider", "local"));
            file.setBucketName("local");
            file.setBizType(StringUtils.hasText(bizType)
                    ? bizType
                    : systemConfigService.getString("file.defaultBizType", "default"));
            file.setMd5(DigestUtils.md5DigestAsHex(bytes));
            file.setStatus(1);
            save(file);
            file.setUrl("/api/file/download/" + file.getId());
            updateById(file);
            return file;
        } catch (IOException ex) {
            throw new BusinessException(500, "文件上传失败");
        }
    }

    @Override
    public Resource loadAsResource(String id) {
        File file = getById(id);
        if (file == null) {
            throw new BusinessException(404, "文件不存在");
        }
        Path root = Paths.get(baseConfig.getPath().getUpload()).toAbsolutePath().normalize();
        Path target = root.resolve(file.getObjectName()).normalize();
        if (!Files.exists(target)) {
            throw new BusinessException(404, "文件内容不存在");
        }
        return new PathResource(target);
    }

    @Override
    public boolean removeFileWithStorage(String id) {
        File file = getById(id);
        if (file == null) {
            return true;
        }
        Path root = Paths.get(baseConfig.getPath().getUpload()).toAbsolutePath().normalize();
        Path target = root.resolve(file.getObjectName()).normalize();
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new BusinessException(500, "文件删除失败");
        }
        return removeById(id);
    }

    @Override
    public boolean removeFilesWithStorage(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        for (String id : ids) {
            removeFileWithStorage(id);
        }
        return true;
    }

    @Override
    public FileCenterStatsVO getCenterStats() {
        List<File> files = list(new LambdaQueryWrapper<File>()
                .select(
                        File::getId,
                        File::getFileSize,
                        File::getBizType,
                        File::getStorageProvider,
                        File::getCreateTime
                ));

        LocalDate today = LocalDate.now();
        long totalSize = files.stream()
                .map(File::getFileSize)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        List<File> todayFiles = files.stream()
                .filter(file -> file.getCreateTime() != null)
                .filter(file -> file.getCreateTime().toLocalDate().isEqual(today))
                .toList();
        long todaySize = todayFiles.stream()
                .map(File::getFileSize)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        FileCenterStatsVO stats = new FileCenterStatsVO();
        stats.setTotalCount((long) files.size());
        stats.setTotalSize(totalSize);
        stats.setTodayUploadCount((long) todayFiles.size());
        stats.setTodayUploadSize(todaySize);
        stats.setStorageProvider(systemConfigService.getString("file.storageProvider", "local"));
        stats.setDefaultBizType(systemConfigService.getString("file.defaultBizType", "system"));
        stats.setMaxSizeMb(systemConfigService.getInt("file.maxSizeMb", 20));
        stats.setAllowedTypes(Arrays.stream(Objects.requireNonNullElse(
                        systemConfigService.getString("file.allowedTypes", ""),
                        ""
                ).split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList());

        List<FileCenterMetricVO> bizTypeMetrics = buildMetricList(files, true);
        List<FileCenterMetricVO> providerMetrics = buildMetricList(files, false);
        stats.setBizTypeMetrics(bizTypeMetrics);
        stats.setProviderMetrics(providerMetrics);
        stats.setBizTypeCount(bizTypeMetrics.size());
        stats.setProviderCount(providerMetrics.size());
        return stats;
    }

    private List<FileCenterMetricVO> buildMetricList(List<File> files, boolean byBizType) {
        Map<String, List<File>> grouped = files.stream()
                .collect(Collectors.groupingBy(file -> normalizeMetricLabel(
                        byBizType ? file.getBizType() : file.getStorageProvider(),
                        byBizType ? "未分类" : "未知存储"
                )));

        return grouped.entrySet().stream()
                .map(entry -> {
                    FileCenterMetricVO metric = new FileCenterMetricVO();
                    metric.setLabel(entry.getKey());
                    metric.setCount((long) entry.getValue().size());
                    metric.setTotalSize(entry.getValue().stream()
                            .map(File::getFileSize)
                            .filter(Objects::nonNull)
                            .mapToLong(Long::longValue)
                            .sum());
                    return metric;
                })
                .sorted(Comparator.comparing(FileCenterMetricVO::getCount).reversed()
                        .thenComparing(FileCenterMetricVO::getLabel))
                .toList();
    }

    private String normalizeMetricLabel(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        return value.trim();
    }
}
