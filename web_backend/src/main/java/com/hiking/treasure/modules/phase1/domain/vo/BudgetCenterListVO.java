package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 预算中心 - 列表项")
public class BudgetCenterListVO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "项目编码")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "预算计划ID")
    private String budgetPlanId;

    @Schema(description = "预算计划名称")
    private String planName;

    @Schema(description = "币种")
    private String currencyCode;

    @Schema(description = "预算健康状态")
    private String budgetStatus;

    @Schema(description = "总预算")
    private Long totalBudgetAmount;

    @Schema(description = "锁定预算")
    private Long lockedAmount;

    @Schema(description = "已消耗预算")
    private Long consumedAmount;

    @Schema(description = "待增补预算")
    private Long pendingIncreaseAmount;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdatedTime;
}
