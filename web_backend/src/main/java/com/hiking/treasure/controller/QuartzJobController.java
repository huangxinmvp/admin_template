package com.hiking.treasure.controller;

import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.QuartzJob;
import com.hiking.treasure.service.QuartzJobService;
import com.hiking.treasure.domain.dto.create.QuartzJobCreateDTO;
import com.hiking.treasure.domain.dto.update.QuartzJobUpdateDTO;
import com.hiking.treasure.domain.dto.query.QuartzJobQueryDTO;
import com.hiking.treasure.domain.vo.QuartzJobVO;
import com.hiking.treasure.domain.convert.QuartzJobConvert;
import com.hiking.treasure.common.web.BaseController;
import com.hiking.treasure.common.api.vo.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.servlet.ModelAndView;

/**
 * 定时任务 - 控制器
 */
@Tag(name = "定时任务", description = "定时任务接口" )
@RestController
@RequestMapping("/api/quartzJob" )
public class QuartzJobController extends BaseController<QuartzJob, QuartzJobService>{

    @Resource
    private QuartzJobService quartzJobService;

    @Resource
    private QuartzJobConvert quartzJobConvert;


    @Operation(summary = "分页查询" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:view')")
    @GetMapping("/page" )
    public Result<Page<QuartzJobVO>> page(@Valid QuartzJobQueryDTO dto, @RequestParam(defaultValue = "1") long pageNo, @RequestParam(defaultValue = "10") long pageSize) {
    LambdaQueryWrapper<QuartzJob> qw = new LambdaQueryWrapper<>();
    QuartzJob cond = quartzJobConvert.toEntity(dto);
    qw.setEntity(cond);

    Page<QuartzJob> page = new Page<>(pageNo, pageSize);
    IPage<QuartzJob> entityPage = quartzJobService.page(page, qw);

    List<QuartzJobVO> voList = quartzJobConvert.toVOs(entityPage.getRecords());
    Page<QuartzJobVO> voPage = new Page<>(pageNo, pageSize, entityPage.getTotal());
    voPage.setRecords(voList);

    return Result.ok(voPage);
}

    @Operation(summary = "新增" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:create')")
    @PostMapping
    public Result<QuartzJobVO> create(@Valid @RequestBody QuartzJobCreateDTO dto) {
        QuartzJob entity = quartzJobConvert.toEntity(dto);
        quartzJobService.saveJob(entity);
        return Result.ok(quartzJobConvert.toVO(entity));
    }

    @Operation(summary = "更新" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:update')")
    @PutMapping("/{id}" )
    public Result<Boolean> update(@PathVariable String id,
            @Valid @RequestBody QuartzJobUpdateDTO dto) {
        QuartzJob entity = quartzJobConvert.toEntity(dto);
        entity.setId(id);
        return Result.ok(quartzJobService.updateJob(entity));
    }

    @Operation(summary = "删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:delete')")
    @DeleteMapping("/{id}" )
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(quartzJobService.removeById(id));
    }

    @Operation(summary = "批量删除" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:delete')")
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestParam("ids") String ids) {
        return Result.ok(quartzJobService.removeByIds(Arrays.asList(ids.split(","))));
    }

    @Operation(summary = "详情" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:view')")
    @GetMapping("/{id}" )
    public Result<QuartzJobVO> detail(@PathVariable String id) {
        QuartzJob entity = quartzJobService.getById(id);
        return Result.ok(quartzJobConvert.toVO(entity));
    }

    @Operation(summary = "导出Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:view')")
    @GetMapping("/exportXls" )
    public ModelAndView exportXls(HttpServletRequest request, QuartzJobQueryDTO dto) {
        QuartzJob cond = quartzJobConvert.toEntity(dto);
        String exportFields = null;
        Integer pageNumPerSheet = 500;
        return super.exportXlsSheet(request, cond, QuartzJob.class, "定时任务", exportFields, pageNumPerSheet);
    }

    @Operation(summary = "导入Excel" )
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:create')")
    @PostMapping("/importExcel" )
    public Result<String> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, QuartzJob.class);
    }

    @Operation(summary = "手动触发任务")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:job:trigger')")
    @PostMapping("/{id}/trigger")
    public Result<Boolean> trigger(@PathVariable String id) {
        return Result.ok(quartzJobService.triggerNow(id));
    }

    @Override
    protected QuartzJobService service() {
        return this.quartzJobService;
    }
}
