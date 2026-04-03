package com.hiking.treasure.modules.phase1.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 预算流水 - 更新DTO")
public class BudgetLedgerUpdateDTO {

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

    @Schema(description = "发生时间")
    private LocalDateTime occurredAt;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "备注")
    private String remark;
}
