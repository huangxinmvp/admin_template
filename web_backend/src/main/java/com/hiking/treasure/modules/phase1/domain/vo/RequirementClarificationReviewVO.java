package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "需求澄清协作审阅 VO")
public class RequirementClarificationReviewVO {

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
    private List<String> roleInsights;

    @Schema(description = "选择建议")
    private String selectionGuidance;

    @Schema(description = "治理解释")
    private String governanceInterpretation;

    @Schema(description = "是否建议提升到决策")
    private Integer decisionEscalationAdvised;

    @Schema(description = "后续治理建议")
    private List<String> followUpHints;

    @Schema(description = "阻塞评估")
    private String blockerAssessment;

    @Schema(description = "下一轮问题")
    private List<String> nextQuestions;

    @Schema(description = "合并后的澄清建议")
    private List<ClarificationSuggestionVO> suggestions;
}
