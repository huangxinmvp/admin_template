package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目中心 - 治理汇总")
public class ProjectGovernanceSummaryVO {

    @Schema(description = "当前阶段编码")
    private String currentStageCode;

    @Schema(description = "当前阶段名称")
    private String currentStageName;

    @Schema(description = "当前阶段状态")
    private String currentStageStatus;

    @Schema(description = "当前门禁状态")
    private String currentGateStatus;

    @Schema(description = "整体治理状态")
    private String governanceStatus;

    @Schema(description = "是否阻塞")
    private Integer blockedFlag;

    @Schema(description = "是否存在风险")
    private Integer atRiskFlag;

    @Schema(description = "待处理决策事项数")
    private Integer pendingDecisionItemCount;

    @Schema(description = "待处理审批数")
    private Integer pendingApprovalCount;

    @Schema(description = "阻塞审批数")
    private Integer blockerApprovalCount;

    @Schema(description = "阻塞决策事项数")
    private Integer blockerDecisionCount;

    @Schema(description = "阻塞门禁条件数")
    private Integer blockingGateConditionCount;

    @Schema(description = "失败门禁条件数")
    private Integer failedGateConditionCount;

    @Schema(description = "缺失关键角色数")
    private Integer missingCriticalRoleCount;

    @Schema(description = "预算健康状态")
    private String budgetStatus;

    @Schema(description = "需求完整度")
    private Integer requirementCompletenessScore;

    @Schema(description = "澄清项总数")
    private Integer clarificationCount;

    @Schema(description = "阻塞澄清项数")
    private Integer blockerClarificationCount;

    @Schema(description = "治理原因摘要")
    private String blockerReasonSummary;

    @Schema(description = "最近治理重算时间")
    private LocalDateTime lastRecomputedAt;
}
