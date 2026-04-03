package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 预算流水 - VO")
public class BudgetLedgerVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "预算计划ID")
    private String budgetPlanId;

    @Schema(description = "流水类型")
    private String entryType;

    @Schema(description = "金额")
    private Long amount;

    @Schema(description = "流水后余额")
    private Long balanceAfter;

    @Schema(description = "关联对象类型")
    private String referenceType;

    @Schema(description = "关联对象ID")
    private String referenceId;

    @Schema(description = "关联对象显示名")
    private String referenceDisplayName;

    @Schema(description = "发生时间")
    private LocalDateTime occurredAt;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
