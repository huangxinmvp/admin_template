package com.hiking.treasure.modules.phase1.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.modules.phase1.domain.dto.query.BudgetCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetCenterListVO;
import com.hiking.treasure.modules.phase1.service.BudgetCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AICoOS 预算中心", description = "AICoOS 预算中心聚合接口")
@RestController
@RequestMapping("/api/aicoos/budgetCenter")
@PreAuthorize("hasRole('ADMIN')")
public class BudgetCenterController {

    @Resource
    private BudgetCenterService budgetCenterService;

    @Operation(summary = "预算中心分页")
    @GetMapping("/page")
    public Result<Page<BudgetCenterListVO>> page(
            @Valid BudgetCenterQueryDTO dto,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(budgetCenterService.pageBudgetCenter(dto, pageNo, pageSize));
    }

    @Operation(summary = "项目预算详情")
    @GetMapping("/project/{projectId}")
    public Result<BudgetCenterDetailVO> detail(@PathVariable String projectId) {
        return Result.ok(budgetCenterService.getProjectBudgetDetail(projectId));
    }
}
