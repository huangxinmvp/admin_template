package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AICoOS 项目中心 - 详情")
public class ProjectCenterDetailVO {

    @Schema(description = "项目基础信息")
    private ProjectVO project;

    @Schema(description = "项目负责人显示名")
    private String ownerDisplayName;

    @Schema(description = "工作流模板名称")
    private String workflowTemplateName;

    @Schema(description = "工作流模板当前阶段编码")
    private String workflowTemplateCurrentStageCode;

    @Schema(description = "工作流模板当前阶段名称")
    private String workflowTemplateCurrentStageName;

    @Schema(description = "治理汇总")
    private ProjectGovernanceSummaryVO governanceSummary;

    @Schema(description = "决策汇总")
    private ProjectDecisionSummaryVO decisionSummary;

    @Schema(description = "审批汇总")
    private ProjectApprovalSummaryVO approvalSummary;

    @Schema(description = "预算汇总")
    private ProjectBudgetSummaryVO budgetSummary;

    @Schema(description = "需求接收汇总")
    private ProjectRequirementSummaryVO requirementSummary;

    @Schema(description = "协作治理联动汇总")
    private ProjectCollaborationSummaryVO collaborationSummary;

    @Schema(description = "下一步建议")
    private ProjectNextStepGuidanceVO nextStepGuidance;

    @Schema(description = "需求接收记录")
    private RequirementIntakeVO requirementIntake;

    @Schema(description = "阶段概览")
    private List<ProjectStageVO> stages;

    @Schema(description = "最近澄清项")
    private List<ClarificationItemVO> recentClarificationItems;

    @Schema(description = "最近决策事项")
    private List<DecisionItemVO> recentDecisionItems;

    @Schema(description = "最近审批记录")
    private List<ApprovalRecordVO> recentApprovalRecords;

    @Schema(description = "最近预算流水")
    private List<BudgetLedgerVO> recentBudgetLedgerEntries;

    @Schema(description = "最近会议记录")
    private List<MeetingRecordVO> recentMeetingRecords;

    @Schema(description = "项目工具绑定")
    private List<ProjectToolBindingVO> projectToolBindings;

    @Schema(description = "最近工具集成审计")
    private List<ToolIntegrationAuditVO> recentToolIntegrationAudits;

    @Schema(description = "关联 Agent 角色")
    private List<ProjectAgentRoleSummaryVO> linkedAgentRoles;

    @Schema(description = "当前阶段推荐角色")
    private List<ProjectAgentRoleSummaryVO> currentStageRecommendedRoles;

    @Schema(description = "缺失关键角色")
    private List<String> missingCriticalRoles;

    @Schema(description = "当前门禁条件")
    private List<ProjectGateConditionVO> currentGateConditions;

    @Schema(description = "最近活动")
    private List<ProjectActivityVO> recentActivities;
}
