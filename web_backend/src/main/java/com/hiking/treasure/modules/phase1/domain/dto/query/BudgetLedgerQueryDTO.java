package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 预算流水 - 查询DTO")
public class BudgetLedgerQueryDTO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "预算计划ID")
    private String budgetPlanId;

    @Schema(description = "流水类型")
    private String entryType;

    @Schema(description = "关联对象类型")
    private String referenceType;
}
