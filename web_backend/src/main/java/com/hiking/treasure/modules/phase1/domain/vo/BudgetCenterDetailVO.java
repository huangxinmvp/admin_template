package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AICoOS 预算中心 - 详情")
public class BudgetCenterDetailVO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "项目编码")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "预算汇总")
    private ProjectBudgetSummaryVO budgetSummary;

    @Schema(description = "最新预算计划")
    private BudgetPlanVO latestBudgetPlan;

    @Schema(description = "角色预算分配")
    private List<BudgetRoleAllocationVO> roleAllocations;

    @Schema(description = "最近预算流水")
    private List<BudgetLedgerVO> recentLedgerEntries;
}
