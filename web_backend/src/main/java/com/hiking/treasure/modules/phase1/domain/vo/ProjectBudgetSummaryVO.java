package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目中心 - 预算汇总")
public class ProjectBudgetSummaryVO {

    @Schema(description = "预算健康状态")
    private String status;

    @Schema(description = "总预算")
    private Long totalBudgetAmount;

    @Schema(description = "预算计划名称")
    private String planName;

    @Schema(description = "币种")
    private String currencyCode;

    @Schema(description = "提议金额")
    private Long proposedAmount;

    @Schema(description = "批准金额")
    private Long approvedAmount;

    @Schema(description = "预留金额")
    private Long reservedAmount;

    @Schema(description = "锁定预算")
    private Long lockedAmount;

    @Schema(description = "已消耗金额")
    private Long consumedAmount;

    @Schema(description = "待增补预算")
    private Long pendingIncreaseAmount;

    @Schema(description = "剩余金额")
    private Long remainingAmount;

    @Schema(description = "最近生效时间")
    private LocalDateTime effectiveAt;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdatedAt;
}
