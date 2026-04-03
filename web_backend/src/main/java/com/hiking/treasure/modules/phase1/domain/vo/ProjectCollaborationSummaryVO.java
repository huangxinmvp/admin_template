package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "AICoOS 项目中心 - 协作治理联动汇总")
public class ProjectCollaborationSummaryVO {

    @Schema(description = "协作治理落地总数")
    private Integer appliedCollaborationCount;

    @Schema(description = "最近协作治理更新时间")
    private LocalDateTime latestCollaborationAt;

    @Schema(description = "治理提示")
    private List<String> governanceHighlights;

    @Schema(description = "需求澄清协作汇总")
    private ProjectRequirementCollaborationSummaryVO requirementClarification;

    @Schema(description = "决策预算协作汇总")
    private ProjectDecisionBudgetCollaborationSummaryVO decisionBudget;

    @Schema(description = "产品架构协作汇总")
    private ProjectProductArchitectureCollaborationSummaryVO productArchitecture;
}
