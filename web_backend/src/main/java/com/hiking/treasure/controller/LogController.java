package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.Log;
import com.hiking.treasure.service.LogService;
import com.hiking.treasure.domain.dto.create.LogCreateDTO;
import com.hiking.treasure.domain.dto.update.LogUpdateDTO;
import com.hiking.treasure.domain.dto.query.LogQueryDTO;
import com.hiking.treasure.domain.vo.LogVO;
import com.hiking.treasure.domain.convert.LogConvert;
import com.hiking.treasure.common.web.BaseController;
import com.hiking.treasure.common.api.vo.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.servlet.ModelAndView;

/**
 * 系统日志 - 控制器
 */
@Tag(name = "系统日志", description = "系统日志接口" )
@RestController
@RequestMapping("/api/log" )
public class LogController extends BaseController<Log, LogService>{

    @Resource
    private LogService logService;

    @Resource
    private LogConvert logConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<LogVO>> page(@Valid LogQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<Log> qw = new LambdaQueryWrapper<>();
    Log cond = logConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<Log> page = new Page<>(pageNo, pageSize);
    IPage<Log> entityPage = logService.page(page, qw);

    List<LogVO> voList = logConvert.toVOs(entityPage.getRecords());
    Page<LogVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<LogVO> create(@Valid @RequestBody LogCreateDTO dto) {
        Log entity = logConvert.toEntity(dto);
        logService.save(entity);
        return Result.ok(logConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody LogUpdateDTO dto) {
        Log entity = logConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(logService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(logService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(logService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<LogVO> detail(@PathVariable String id) {
        Log entity = logService.getById(id);
        return Result.ok(logConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, LogQueryDTO dto) {
        Log cond = logConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, Log.class, "系统日志", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Log.class);
    }

    @Override
    protected LogService service() {
        return this.logService;
    }
}
