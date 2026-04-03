package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "应用协作式决策预算评审 DTO")
public class DecisionBudgetReviewApplyDTO {

    @Schema(description = "决策建议")
    private String decisionRecommendation;

    @Schema(description = "预算影响说明")
    private String budgetImpactNote;

    @Schema(description = "是否建议预算确认")
    private Integer budgetConfirmationAdvised;

    @Schema(description = "推荐选项")
    private String recommendedOption;

    @Schema(description = "项目影响说明")
    private String projectImpactNote;

    @Schema(description = "阻塞评估")
    private String blockerAssessment;

    @Schema(description = "下一步建议")
    private List<String> nextSteps;

    @Schema(description = "决策联动说明")
    private String decisionLinkageSummary;

    @Schema(description = "预算联动说明")
    private String budgetLinkageSummary;

    @Schema(description = "后续治理建议")
    private List<String> followUpHints;
}
