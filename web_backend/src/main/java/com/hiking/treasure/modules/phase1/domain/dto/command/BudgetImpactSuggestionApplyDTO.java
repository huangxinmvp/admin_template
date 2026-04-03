package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "应用预算影响建议 DTO")
public class BudgetImpactSuggestionApplyDTO {

    @Schema(description = "预算影响摘要")
    private String budgetImpactSummary;

    @Schema(description = "项目影响摘要")
    private String projectImpactSummary;

    @Schema(description = "预算变化范围")
    private String deltaRange;

    @Schema(description = "受影响角色")
    private List<String> affectedRoles;

    @Schema(description = "可信度")
    private String confidence;
}
