package com.hiking.treasure.modules.phase1.service.impl;

import com.hiking.treasure.modules.phase1.domain.vo.ProjectAgentRoleSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectApprovalSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectBudgetSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectDecisionBudgetCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectDecisionSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectGateConditionVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectGovernanceSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectNextStepGuidanceVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectProductArchitectureCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectRequirementCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectRequirementSummaryVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectNextStepGuidanceResolverTest {

    private final ProjectNextStepGuidanceResolver resolver = new ProjectNextStepGuidanceResolver();

    @Test
    void blockerClarificationOverridesOpenDecisionsAndBudgetWarning() {
        ProjectGovernanceSummaryVO governance = governance("clarification");
        governance.setBlockerClarificationCount(2);
        governance.setBlockerDecisionCount(1);
        governance.setBudgetStatus("warning");

        ProjectRequirementSummaryVO requirement = new ProjectRequirementSummaryVO();
        requirement.setHasRequirementIntake(true);
        requirement.setCompletenessScore(88);
        requirement.setBlockerCount(2);

        ProjectDecisionSummaryVO decision = new ProjectDecisionSummaryVO();
        decision.setBlockerCount(1);

        ProjectBudgetSummaryVO budget = new ProjectBudgetSummaryVO();
        budget.setStatus("warning");

        ProjectNextStepGuidanceVO guidance = resolver.resolve(context(
                governance,
                requirement,
                decision,
                null,
                budget,
                null,
                List.of(),
                List.of(),
                List.of(role("需求分析师", "clarification_manage"))
        ));

        assertEquals("clarification-center", guidance.getRecommendedTargetModule());
        assertEquals("urgent", guidance.getRecommendedPriority());
        assertEquals("需求分析师", guidance.getRecommendedActorRole());
        assertTrue(guidance.getCurrentBlockers().contains("阻塞澄清项 2"));
        assertEquals(3, guidance.getPriorityActions().size());
        assertEquals(1, guidance.getPriorityActions().get(0).getActionOrder());
        assertEquals("在澄清中心优先关闭阻塞澄清项", guidance.getPriorityActions().get(0).getActionLabel());
        assertEquals("clarification-center", guidance.getPriorityActions().get(0).getTargetModule());
        assertNotNull(guidance.getPriorityActions().get(0).getRecommendedReason());
        assertNotNull(guidance.getPriorityActions().get(0).getAddressedRisk());
        assertNotNull(guidance.getPriorityActions().get(0).getExpectedChange());
    }

    @Test
    void blockerDecisionOverridesNonBlockingApprovalState() {
        ProjectGovernanceSummaryVO governance = governance("planning");
        governance.setBlockerDecisionCount(1);
        governance.setPendingApprovalCount(2);
        governance.setBudgetStatus("healthy");

        ProjectDecisionSummaryVO decision = new ProjectDecisionSummaryVO();
        decision.setBlockerCount(1);

        ProjectApprovalSummaryVO approval = new ProjectApprovalSummaryVO();
        approval.setPendingCount(2);
        approval.setBlockerCount(0);

        ProjectNextStepGuidanceVO guidance = resolver.resolve(context(
                governance,
                null,
                decision,
                approval,
                null,
                null,
                List.of(),
                List.of(),
                List.of(role("产品经理", "decision_create"))
        ));

        assertEquals("decision-center", guidance.getRecommendedTargetModule());
        assertEquals("urgent", guidance.getRecommendedPriority());
        assertEquals("产品经理", guidance.getRecommendedActorRole());
    }

    @Test
    void blockingApprovalProducesApprovalGuidance() {
        ProjectGovernanceSummaryVO governance = governance("approval");
        governance.setBlockerApprovalCount(2);

        ProjectApprovalSummaryVO approval = new ProjectApprovalSummaryVO();
        approval.setBlockerCount(2);

        ProjectNextStepGuidanceVO guidance = resolver.resolve(context(
                governance,
                null,
                null,
                approval,
                null,
                null,
                List.of(),
                List.of(),
                List.of(role("PMO", "approval_request"))
        ));

        assertEquals("approval-center", guidance.getRecommendedTargetModule());
        assertEquals("urgent", guidance.getRecommendedPriority());
        assertEquals("PMO", guidance.getRecommendedActorRole());
    }

    @Test
    void failedGateProducesGateGuidance() {
        ProjectGovernanceSummaryVO governance = governance("design");
        governance.setFailedGateConditionCount(1);

        ProjectGateConditionVO gateCondition = new ProjectGateConditionVO();
        gateCondition.setTitle("方案门禁");
        gateCondition.setStatus("blocked");
        gateCondition.setSummary("缺少方案确认");

        ProjectNextStepGuidanceVO guidance = resolver.resolve(context(
                governance,
                null,
                null,
                null,
                null,
                null,
                List.of(gateCondition),
                List.of(),
                List.of(role("交付负责人", "stage_suggestion_update"))
        ));

        assertEquals("project-center", guidance.getRecommendedTargetModule());
        assertEquals("urgent", guidance.getRecommendedPriority());
        assertEquals("交付负责人", guidance.getRecommendedActorRole());
    }

    @Test
    void budgetStatusesProduceBudgetGuidance() {
        for (String budgetStatus : List.of("warning", "pending", "overrun", "unplanned")) {
            ProjectGovernanceSummaryVO governance = governance("estimation");
            governance.setBudgetStatus(budgetStatus);

            ProjectBudgetSummaryVO budget = new ProjectBudgetSummaryVO();
            budget.setStatus(budgetStatus);

            ProjectNextStepGuidanceVO guidance = resolver.resolve(context(
                    governance,
                    null,
                    null,
                    null,
                    budget,
                    null,
                    List.of(),
                    List.of(),
                    List.of(role("预算分析师", "budget_change_propose"))
            ));

            assertEquals("budget-center", guidance.getRecommendedTargetModule());
            assertEquals("预算分析师", guidance.getRecommendedActorRole());
            assertTrue(List.of("urgent", "high").contains(guidance.getRecommendedPriority()));
        }
    }

    @Test
    void missingCriticalRoleProducesRoleGuidance() {
        ProjectGovernanceSummaryVO governance = governance("development");
        governance.setMissingCriticalRoleCount(1);

        ProjectNextStepGuidanceVO guidance = resolver.resolve(context(
                governance,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of("当前阶段未配置必需的 Agent 角色"),
                List.of()
        ));

        assertEquals("agent-roles", guidance.getRecommendedTargetModule());
        assertEquals("PMO / 资源协调", guidance.getRecommendedActorRole());
        assertEquals("high", guidance.getRecommendedPriority());
    }

    @Test
    void collaborationFollowUpIsUsedWhenNoHardBlockerExists() {
        ProjectGovernanceSummaryVO governance = governance("clarification");
        governance.setBudgetStatus("healthy");

        ProjectRequirementCollaborationSummaryVO requirementCollaboration = new ProjectRequirementCollaborationSummaryVO();
        requirementCollaboration.setDecisionEscalationSuggestionCount(2);

        ProjectCollaborationSummaryVO collaboration = new ProjectCollaborationSummaryVO();
        collaboration.setRequirementClarification(requirementCollaboration);

        ProjectNextStepGuidanceVO guidance = resolver.resolve(context(
                governance,
                null,
                null,
                null,
                null,
                collaboration,
                List.of(),
                List.of(),
                List.of(role("产品经理", "decision_create"))
        ));

        assertEquals("decision-center", guidance.getRecommendedTargetModule());
        assertEquals("high", guidance.getRecommendedPriority());
        assertEquals("产品经理", guidance.getRecommendedActorRole());
        assertEquals(3, guidance.getPriorityActions().size());
        assertEquals("回看建议升级的澄清项", guidance.getPriorityActions().get(0).getActionLabel());
        assertEquals("clarification-center", guidance.getPriorityActions().get(0).getTargetModule());
        assertEquals(2, guidance.getPriorityActions().get(1).getActionOrder());
    }

    @Test
    void healthyProjectFallsBackToStageProgressGuidance() {
        ProjectGovernanceSummaryVO governance = governance("intake");
        governance.setBudgetStatus("healthy");

        ProjectRequirementSummaryVO requirement = new ProjectRequirementSummaryVO();
        requirement.setHasRequirementIntake(false);
        requirement.setCompletenessScore(40);

        ProjectNextStepGuidanceVO guidance = resolver.resolve(context(
                governance,
                requirement,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(role("需求分析师", "clarification_manage"))
        ));

        assertEquals("requirement-intake", guidance.getRecommendedTargetModule());
        assertEquals("normal", guidance.getRecommendedPriority());
        assertEquals("需求分析师", guidance.getRecommendedActorRole());
        assertEquals("回看业务目标、功能摘要和预算范围", guidance.getPriorityActions().get(0).getActionLabel());
        assertEquals("处理需求基础信息不完整的风险", guidance.getPriorityActions().get(0).getAddressedRisk());
    }

    private ProjectNextStepGuidanceResolver.ProjectNextStepContext context(
            ProjectGovernanceSummaryVO governance,
            ProjectRequirementSummaryVO requirement,
            ProjectDecisionSummaryVO decision,
            ProjectApprovalSummaryVO approval,
            ProjectBudgetSummaryVO budget,
            ProjectCollaborationSummaryVO collaboration,
            List<ProjectGateConditionVO> gateConditions,
            List<String> missingRoles,
            List<ProjectAgentRoleSummaryVO> currentStageRoles) {
        return new ProjectNextStepGuidanceResolver.ProjectNextStepContext(
                governance == null ? null : governance.getCurrentStageCode(),
                governance == null ? null : governance.getCurrentStageName(),
                null,
                governance,
                requirement,
                decision,
                approval,
                budget,
                collaboration,
                gateConditions,
                missingRoles,
                currentStageRoles
        );
    }

    private ProjectGovernanceSummaryVO governance(String stageCode) {
        ProjectGovernanceSummaryVO governance = new ProjectGovernanceSummaryVO();
        governance.setCurrentStageCode(stageCode);
        governance.setCurrentStageName(stageCode);
        governance.setBudgetStatus("healthy");
        governance.setGovernanceStatus("healthy");
        return governance;
    }

    private ProjectAgentRoleSummaryVO role(String roleName, String... actionCodes) {
        ProjectAgentRoleSummaryVO role = new ProjectAgentRoleSummaryVO();
        role.setRoleName(roleName);
        role.setCurrentStageParticipationType("required");
        role.setAllowedActionCodes(List.of(actionCodes));
        return role;
    }
}
