package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目中心 - 决策预算协作汇总")
public class ProjectDecisionBudgetCollaborationSummaryVO {

    @Schema(description = "已应用协作评审的决策数")
    private Integer appliedDecisionCount;

    @Schema(description = "仍待推进的协作决策数")
    private Integer openDecisionCount;

    @Schema(description = "阻塞型协作决策数")
    private Integer blockerDecisionCount;

    @Schema(description = "建议补充预算确认次数")
    private Integer budgetConfirmationSuggestedCount;

    @Schema(description = "最近评审决策标题")
    private String latestDecisionTitle;

    @Schema(description = "最近推荐选项")
    private String latestRecommendedOption;

    @Schema(description = "最近应用时间")
    private LocalDateTime latestAppliedAt;
}
