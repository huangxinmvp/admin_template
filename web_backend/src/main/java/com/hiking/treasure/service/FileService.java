package com.hiking.treasure.service;

import com.hiking.treasure.domain.vo.FileCenterStatsVO;
import com.hiking.treasure.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 文件存储记录 服务类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface FileService extends IService<File> {
    File upload(MultipartFile multipartFile, String bizType);
    Resource loadAsResource(String id);
    boolean removeFileWithStorage(String id);
    boolean removeFilesWithStorage(List<String> ids);
    FileCenterStatsVO getCenterStats();
}
