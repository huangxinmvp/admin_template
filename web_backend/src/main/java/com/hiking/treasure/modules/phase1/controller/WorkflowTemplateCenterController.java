package com.hiking.treasure.modules.phase1.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.modules.phase1.domain.dto.command.WorkflowTemplateCenterSaveDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.WorkflowTemplateCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateCenterListVO;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AICoOS 工作流模板中心", description = "AICoOS 工作流模板中心聚合接口")
@RestController
@RequestMapping("/api/aicoos/workflowTemplate/center")
@PreAuthorize("hasRole('ADMIN')")
public class WorkflowTemplateCenterController {

    @Resource
    private WorkflowTemplateService workflowTemplateService;

    @Operation(summary = "工作流模板中心分页")
    @GetMapping("/page")
    public Result<Page<WorkflowTemplateCenterListVO>> page(
            @Valid WorkflowTemplateCenterQueryDTO dto,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(workflowTemplateService.pageTemplateCenter(dto, pageNo, pageSize));
    }

    @Operation(summary = "工作流模板中心详情")
    @GetMapping("/{id}")
    public Result<WorkflowTemplateCenterDetailVO> detail(@PathVariable String id) {
        return Result.ok(workflowTemplateService.getTemplateCenterDetail(id));
    }

    @Operation(summary = "创建工作流模板")
    @PostMapping
    public Result<WorkflowTemplateCenterDetailVO> create(@RequestBody WorkflowTemplateCenterSaveDTO dto) {
        return Result.ok(workflowTemplateService.createTemplateCenter(dto));
    }

    @Operation(summary = "更新工作流模板")
    @PutMapping("/{id}")
    public Result<WorkflowTemplateCenterDetailVO> update(
            @PathVariable String id,
            @RequestBody WorkflowTemplateCenterSaveDTO dto) {
        return Result.ok(workflowTemplateService.updateTemplateCenter(id, dto));
    }
}
