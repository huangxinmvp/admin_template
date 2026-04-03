package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "决策与预算协作审阅 VO")
public class DecisionBudgetReviewVO {

    @Schema(description = "参与角色")
    private List<String> participants;

    @Schema(description = "场景标签")
    private String scenarioLabel;

    @Schema(description = "协作摘要")
    private String collaborationSummary;

    @Schema(description = "推荐操作")
    private String recommendedOperatorAction;

    @Schema(description = "治理联动信息")
    private java.util.Map<String, Object> governanceLinkage;

    @Schema(description = "角色视角")
    private java.util.Map<String, String> roleInsights;

    @Schema(description = "风险标记")
    private List<String> riskFlags;

    @Schema(description = "决策联动说明")
    private String decisionLinkageSummary;

    @Schema(description = "预算联动说明")
    private String budgetLinkageSummary;

    @Schema(description = "后续治理建议")
    private List<String> followUpHints;

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
}
