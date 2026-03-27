package com.hiking.treasure.common.web;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.config.BaseConfig;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 基类控制器（方法签名对齐 JeecgController，仅保留必要导入导出）
 */
@Slf4j
public abstract class BaseController<T, S extends IService<T>> {

    protected abstract S service();

    @Resource
    private BaseConfig baseConfig;

    /**
     * 导出 Excel（签名对齐）
     */
    protected ModelAndView exportXls(HttpServletRequest request, T object, Class<T> clazz, String title) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        String selections = request.getParameter("selections" );
        if (selections != null && !selections.isBlank()) {
            wrapper.in("id", Arrays.asList(selections.split("," )));
        }
        List<T> data = service().list(wrapper);
        writeExcelToResponse(getResponse(), title, clazz, data);
        return null; // 已直接写回响应流
    }

    /**
     * 导出（带 exportFields 参数的同名重载，保持签名一致）
     */
    protected ModelAndView exportXls(HttpServletRequest request, T object, Class<T> clazz, String title, String exportFields) {
        return exportXls(request, object, clazz, title);
    }

    protected ModelAndView exportXlsSheet(HttpServletRequest request,
                                          T object,
                                          Class<T> clazz,
                                          String title,
                                          String exportFields,
                                          Integer pageNum) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1000;
        }

        // 1) 组查询（仅处理 selections）
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        String selections = request.getParameter("selections" );
        if (selections != null && !selections.isBlank()) {
            wrapper.in("id", Arrays.asList(selections.split("," )));
        }
        List<T> all = service().list(wrapper);

        // 2) 准备响应头
        HttpServletResponse response = getResponse();
        prepareDownloadHeaders(response, title);

        // 3) 打开输出流：这里可能抛 IOException —— 单独 try-with-resources 捕获并转为 500
        try (ServletOutputStream out = response.getOutputStream()) {
            com.alibaba.excel.ExcelWriter writer = null;
            try {
                writer = EasyExcel.write(out, clazz)
                        .autoCloseStream(false) // 由 try-with-resources 关闭 out
                        .build();

                int total = all.size();
                int sheetIdx = 0;
                for (int start = 0; start < total; start += pageNum) {
                    int end = Math.min(start + pageNum, total);
                    List<T> slice = all.subList(start, end);
                    writer.write(
                            slice,
                            EasyExcel.writerSheet(sheetIdx, title + (sheetIdx + 1)).build()
                    );
                    sheetIdx++;
                }
                out.flush();
            } catch (RuntimeException ex) {
                // EasyExcel 在写入阶段出错
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Export failed", ex);
            } finally {
                if (writer != null) {
                    try {
                        writer.finish();
                    } catch (Exception ignore) {
                    }
                }
            }
        } catch (java.io.IOException io) {
            // 打开/写入输出流失败
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "I/O error during export", io);
        }

        // 已写回响应流
        return null;
    }


    /**
     * 通过 Excel 导入数据（签名对齐）
     */
    protected Result<String> importExcel(HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        try {
            if (!(request instanceof MultipartHttpServletRequest multipart)) {
                return Result.error("未检测到上传文件" );
            }
            Map<String, MultipartFile> files = multipart.getFileMap();
            for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                var file = entry.getValue();
                List<T> list = new ArrayList<>();
                EasyExcel.read(file.getInputStream(), clazz,
                                new com.alibaba.excel.read.listener.PageReadListener<T>(list::addAll))
                        .autoCloseStream(true).sheet().doRead();
                if (!list.isEmpty()) {
                    service().saveBatch(list);
                }
                return Result.ok("文件导入成功！数据行数：" + list.size());
            }
            return Result.error("未选择文件" );
        } catch (Exception e) {
            log.error("导入失败", e);
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate entry" )) {
                return Result.error("文件导入失败: 有重复数据！" );
            }
            return Result.error("文件导入失败: " + msg);
        }
    }

    // ===== 内部辅助 =====
    private static HttpServletResponse getResponse() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) throw new IllegalStateException("No request context" );
        return attrs.getResponse();
    }

    private static <E> void writeExcelToResponse(HttpServletResponse response, String title, Class<E> clazz, List<E> data) {
        prepareDownloadHeaders(response, title);
        try {
            EasyExcel.write(response.getOutputStream(), clazz)
                    .autoCloseStream(false)
                    .sheet(title)
                    .doWrite(data);
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    private static void prepareDownloadHeaders(HttpServletResponse response, String title) {
        try {
            String fileName = URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "%20" );
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" );
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx" );
        } catch (Exception e) {
            throw new RuntimeException("设置响应头失败", e);
        }
    }
}
