package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.QuartzJobLog;
import com.hiking.treasure.service.QuartzJobLogService;
import com.hiking.treasure.domain.dto.create.QuartzJobLogCreateDTO;
import com.hiking.treasure.domain.dto.update.QuartzJobLogUpdateDTO;
import com.hiking.treasure.domain.dto.query.QuartzJobLogQueryDTO;
import com.hiking.treasure.domain.vo.QuartzJobLogVO;
import com.hiking.treasure.domain.convert.QuartzJobLogConvert;
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
 * 定时任务执行日志 - 控制器
 */
@Tag(name = "定时任务执行日志", description = "定时任务执行日志接口" )
@RestController
@RequestMapping("/api/quartzJobLog" )
public class QuartzJobLogController extends BaseController<QuartzJobLog, QuartzJobLogService>{

    @Resource
    private QuartzJobLogService quartzJobLogService;

    @Resource
    private QuartzJobLogConvert quartzJobLogConvert;


    @Operation(summary = "分页查询" )
    @GetMapping("/page" )
    public Result<Page<QuartzJobLogVO>> page(@Valid QuartzJobLogQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<QuartzJobLog> qw = new LambdaQueryWrapper<>();
    QuartzJobLog cond = quartzJobLogConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<QuartzJobLog> page = new Page<>(pageNo, pageSize);
    IPage<QuartzJobLog> entityPage = quartzJobLogService.page(page, qw);

    List<QuartzJobLogVO> voList = quartzJobLogConvert.toVOs(entityPage.getRecords());
    Page<QuartzJobLogVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PostMapping
    public Result<QuartzJobLogVO> create(@Valid @RequestBody QuartzJobLogCreateDTO dto) {
        QuartzJobLog entity = quartzJobLogConvert.toEntity(dto);
        quartzJobLogService.save(entity);
        return Result.ok(quartzJobLogConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody QuartzJobLogUpdateDTO dto) {
        QuartzJobLog entity = quartzJobLogConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(quartzJobLogService.updateById(entity));
    }

    @Operation(summary = "删除" )
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(quartzJobLogService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(quartzJobLogService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @GetMapping("/{id}" )
    public Result<QuartzJobLogVO> detail(@PathVariable String id) {
        QuartzJobLog entity = quartzJobLogService.getById(id);
        return Result.ok(quartzJobLogConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, QuartzJobLogQueryDTO dto) {
        QuartzJobLog cond = quartzJobLogConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, QuartzJobLog.class, "定时任务执行日志", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, QuartzJobLog.class);
    }

    @Override
    protected QuartzJobLogService service() {
        return this.quartzJobLogService;
    }
}
