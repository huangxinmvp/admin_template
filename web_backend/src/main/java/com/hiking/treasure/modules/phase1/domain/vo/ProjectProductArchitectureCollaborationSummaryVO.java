package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目中心 - 产品架构协作汇总")
public class ProjectProductArchitectureCollaborationSummaryVO {

    @Schema(description = "协作简报数量")
    private Integer briefCount;

    @Schema(description = "最近简报标题")
    private String latestBriefTitle;

    @Schema(description = "最近简报摘要")
    private String latestBriefSummary;

    @Schema(description = "最近开放问题数")
    private Integer latestOpenQuestionCount;

    @Schema(description = "最近行动项数")
    private Integer latestActionItemCount;

    @Schema(description = "最近决策候选数")
    private Integer latestDecisionCandidateCount;

    @Schema(description = "最近保存时间")
    private LocalDateTime latestSavedAt;
}
