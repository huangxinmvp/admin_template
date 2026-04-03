package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "AICoOS 项目中心 - 需求澄清协作汇总")
public class ProjectRequirementCollaborationSummaryVO {

    @Schema(description = "已应用协作澄清数")
    private Integer appliedClarificationCount;

    @Schema(description = "待处理协作澄清数")
    private Integer openClarificationCount;

    @Schema(description = "阻塞级协作澄清数")
    private Integer blockerClarificationCount;

    @Schema(description = "建议升级为决策的协作澄清数")
    private Integer decisionEscalationSuggestionCount;

    @Schema(description = "建议跟进模块")
    private List<String> followUpModules;

    @Schema(description = "最近应用时间")
    private LocalDateTime latestAppliedAt;
}
