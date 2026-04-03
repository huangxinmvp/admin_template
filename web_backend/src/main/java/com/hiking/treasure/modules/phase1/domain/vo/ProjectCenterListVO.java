package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目中心 - 列表项")
public class ProjectCenterListVO {

    @Schema(description = "项目ID")
    private String id;

    @Schema(description = "项目编码")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "项目状态")
    private String status;

    @Schema(description = "治理状态")
    private String governanceStatus;

    @Schema(description = "是否阻塞")
    private Integer blockedFlag;

    @Schema(description = "是否风险")
    private Integer atRiskFlag;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "项目负责人")
    private String ownerDisplayName;

    @Schema(description = "当前阶段编码")
    private String currentStageCode;

    @Schema(description = "当前阶段名称")
    private String currentStageName;

    @Schema(description = "当前阶段状态")
    private String currentStageStatus;

    @Schema(description = "当前门禁状态")
    private String currentGateStatus;

    @Schema(description = "预算健康状态")
    private String budgetStatus;

    @Schema(description = "预算批准金额")
    private Long budgetApprovedAmount;

    @Schema(description = "预算已消耗金额")
    private Long budgetConsumedAmount;

    @Schema(description = "预算剩余金额")
    private Long budgetRemainingAmount;

    @Schema(description = "待处理决策事项数")
    private Integer pendingDecisionItemCount;

    @Schema(description = "阻塞决策事项数")
    private Integer blockerDecisionItemCount;

    @Schema(description = "治理原因摘要")
    private String blockerReasonSummary;

    @Schema(description = "建议下一步")
    private String recommendedNextStep;

    @Schema(description = "建议执行角色")
    private String recommendedActorRole;

    @Schema(description = "建议优先级")
    private String recommendedPriority;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
