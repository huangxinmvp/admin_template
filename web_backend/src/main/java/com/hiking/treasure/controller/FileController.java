package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.domain.vo.FileCenterStatsVO;
import com.hiking.treasure.entity.File;
import com.hiking.treasure.service.FileService;
import com.hiking.treasure.domain.dto.create.FileCreateDTO;
import com.hiking.treasure.domain.dto.update.FileUpdateDTO;
import com.hiking.treasure.domain.dto.query.FileQueryDTO;
import com.hiking.treasure.domain.vo.FileVO;
import com.hiking.treasure.domain.convert.FileConvert;
import com.hiking.treasure.common.web.BaseController;
import com.hiking.treasure.common.api.vo.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件存储记录 - 控制器
 */
@Tag(name = "文件存储记录", description = "文件存储记录接口" )
@RestController
@RequestMapping("/api/file" )
public class FileController extends BaseController<File, FileService>{

    @Resource
    private FileService fileService;

    @Resource
    private FileConvert fileConvert;


    @Operation(summary = "分页查询" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:view')")
    @GetMapping("/page" )
    public Result<Page<FileVO>> page(@Valid FileQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<File> qw = new LambdaQueryWrapper<>();
    File cond = fileConvert.toEntity(dto);
    qw.setEntity(cond);
    qw.orderByDesc(File::getCreateTime);

    Page<File> page = new Page<>(pageNo, pageSize);
    IPage<File> entityPage = fileService.page(page, qw);

    List<FileVO> voList = entityPage.getRecords().stream().map(this::toVO).toList();
    Page<FileVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:create')")
    @PostMapping
    public Result<FileVO> create(@Valid @RequestBody FileCreateDTO dto) {
        File entity = fileConvert.toEntity(dto);
        fileService.save(entity);
        return Result.ok(toVO(entity));
    }

    @Operation(summary = "更新" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:update')")
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody FileUpdateDTO dto) {
        File entity = fileConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(fileService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:delete')")
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(fileService.removeFileWithStorage(id));
    }

    @Operation(summary = "批量删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:delete')")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(fileService.removeFilesWithStorage(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:view')")
    @GetMapping("/{id}" )
    public Result<FileVO> detail(@PathVariable String id) {
        File entity = fileService.getById(id);
        return Result.ok(toVO(entity));
    }

    @Operation(summary = "文件中心统计")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:view')")
    @GetMapping("/stats")
    public Result<FileCenterStatsVO> stats() {
        return Result.ok(fileService.getCenterStats());
    }

    @Operation(summary = "导出Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:view')")
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, FileQueryDTO dto) {
        File cond = fileConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, File.class, "文件存储记录", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:create')")
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, File.class);
    }

    @Operation(summary = "上传文件")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:create')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileVO> upload(@RequestPart("file") MultipartFile file,
                                 @RequestParam(value = "bizType", required = false) String bizType) {
        File saved = fileService.upload(file, bizType);
        return Result.ok(toVO(saved));
    }

    @Operation(summary = "下载文件")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:view')")
    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String id) {
        return buildFileResponse(id, false);
    }

    @Operation(summary = "预览文件")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:file:view')")
    @GetMapping("/preview/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> preview(@PathVariable String id) {
        return buildFileResponse(id, true);
    }

    private ResponseEntity<org.springframework.core.io.Resource> buildFileResponse(String id, boolean inline) {
        File file = fileService.getById(id);
        org.springframework.core.io.Resource resource = fileService.loadAsResource(id);
        String filename = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        MediaType mediaType = file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(file.getContentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename*=UTF-8''" + filename)
                .body(resource);
    }

    private FileVO toVO(File entity) {
        FileVO vo = fileConvert.toVO(entity);
        vo.setPreviewUrl("/api/file/preview/" + entity.getId());
        return vo;
    }

    @Override
    protected FileService service() {
        return this.fileService;
    }
}
