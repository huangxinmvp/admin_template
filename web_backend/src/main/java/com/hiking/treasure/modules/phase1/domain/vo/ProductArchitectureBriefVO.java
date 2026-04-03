package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "产品与架构协作简报 VO")
public class ProductArchitectureBriefVO {

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

    @Schema(description = "架构关注点")
    private List<String> architectureFocusAreas;

    @Schema(description = "交付影响")
    private List<String> deliveryImplications;

    @Schema(description = "项目治理联动说明")
    private String projectGovernanceLinkageSummary;

    @Schema(description = "后续治理建议")
    private List<String> followUpHints;

    @Schema(description = "简报标题")
    private String briefTitle;

    @Schema(description = "方案简述")
    private String solutionBrief;

    @Schema(description = "风险列表")
    private List<String> risks;

    @Schema(description = "开放问题")
    private List<String> openQuestions;

    @Schema(description = "下一步建议")
    private List<String> nextSteps;
}
