package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.modules.phase1.domain.convert.ApprovalRecordConvert;
import com.hiking.treasure.modules.phase1.domain.convert.BudgetLedgerConvert;
import com.hiking.treasure.modules.phase1.domain.convert.ClarificationItemConvert;
import com.hiking.treasure.modules.phase1.domain.convert.DecisionItemConvert;
import com.hiking.treasure.modules.phase1.domain.convert.MeetingRecordConvert;
import com.hiking.treasure.modules.phase1.domain.convert.ProjectConvert;
import com.hiking.treasure.modules.phase1.domain.convert.ProjectStageConvert;
import com.hiking.treasure.modules.phase1.domain.convert.ProjectToolBindingConvert;
import com.hiking.treasure.modules.phase1.domain.convert.RequirementIntakeConvert;
import com.hiking.treasure.modules.phase1.domain.convert.ToolIntegrationAuditConvert;
import com.hiking.treasure.modules.phase1.domain.dto.query.ProjectCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterListVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectStageVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectVO;
import com.hiking.treasure.modules.phase1.domain.vo.RequirementIntakeVO;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.entity.ProjectGovernanceState;
import com.hiking.treasure.modules.phase1.entity.ProjectStage;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import com.hiking.treasure.modules.phase1.service.AgentRoleAllowedActionService;
import com.hiking.treasure.modules.phase1.service.AgentRoleService;
import com.hiking.treasure.modules.phase1.service.AgentRoleStageParticipationService;
import com.hiking.treasure.modules.phase1.service.ApprovalRecordService;
import com.hiking.treasure.modules.phase1.service.BudgetLedgerService;
import com.hiking.treasure.modules.phase1.service.BudgetPlanService;
import com.hiking.treasure.modules.phase1.service.ClarificationItemService;
import com.hiking.treasure.modules.phase1.service.DecisionItemService;
import com.hiking.treasure.modules.phase1.service.MeetingRecordService;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.ProjectStageService;
import com.hiking.treasure.modules.phase1.service.ProjectToolBindingService;
import com.hiking.treasure.modules.phase1.service.RequirementIntakeService;
import com.hiking.treasure.modules.phase1.service.ToolIntegrationAuditService;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateService;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateStageService;
import com.hiking.treasure.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectConvert projectConvert;
    @Mock
    private ProjectStageConvert projectStageConvert;
    @Mock
    private RequirementIntakeConvert requirementIntakeConvert;
    @Mock
    private ClarificationItemConvert clarificationItemConvert;
    @Mock
    private DecisionItemConvert decisionItemConvert;
    @Mock
    private MeetingRecordConvert meetingRecordConvert;
    @Mock
    private ApprovalRecordConvert approvalRecordConvert;
    @Mock
    private BudgetLedgerConvert budgetLedgerConvert;
    @Mock
    private ProjectToolBindingConvert projectToolBindingConvert;
    @Mock
    private ToolIntegrationAuditConvert toolIntegrationAuditConvert;
    @Mock
    private ProjectStageService projectStageService;
    @Mock
    private RequirementIntakeService requirementIntakeService;
    @Mock
    private ClarificationItemService clarificationItemService;
    @Mock
    private DecisionItemService decisionItemService;
    @Mock
    private MeetingRecordService meetingRecordService;
    @Mock
    private ApprovalRecordService approvalRecordService;
    @Mock
    private ProjectToolBindingService projectToolBindingService;
    @Mock
    private ToolIntegrationAuditService toolIntegrationAuditService;
    @Mock
    private AgentRoleService agentRoleService;
    @Mock
    private AgentRoleStageParticipationService agentRoleStageParticipationService;
    @Mock
    private AgentRoleAllowedActionService agentRoleAllowedActionService;
    @Mock
    private BudgetPlanService budgetPlanService;
    @Mock
    private BudgetLedgerService budgetLedgerService;
    @Mock
    private WorkflowTemplateService workflowTemplateService;
    @Mock
    private WorkflowTemplateStageService workflowTemplateStageService;
    @Mock
    private ProjectGovernanceLinkageService projectGovernanceLinkageService;
    @Mock
    private UserService userService;

    @Test
    void pageProjectCenterReturnsCompactHintFields() {
        ProjectServiceImpl service = spy(newService());

        Project project = new Project();
        project.setId("p1");
        project.setProjectName("AICoOS 项目");
        project.setProjectCode("AIC-001");
        project.setCurrentStageCode("clarification");
        project.setOwnerUserId("u1");

        Page<Project> projectPage = new Page<>(1, 10, 1);
        projectPage.setRecords(List.of(project));
        doReturn(projectPage).when(service).page(any(Page.class), any(LambdaQueryWrapper.class));

        ProjectGovernanceState state = new ProjectGovernanceState();
        state.setProjectId("p1");
        state.setCurrentStageCode("clarification");
        state.setCurrentStageName("需求澄清");
        state.setBlockedFlag(1);
        state.setAtRiskFlag(0);
        state.setBlockerClarificationCount(2);
        state.setGovernanceStatus("blocked");
        state.setBudgetStatus("healthy");
        when(projectGovernanceLinkageService.loadOrRecomputeProjectStates(List.of("p1")))
                .thenReturn(Map.of("p1", state));
        when(projectStageService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(decisionItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(budgetPlanService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(userService.listByIds(any())).thenReturn(List.of());

        Page<ProjectCenterListVO> result = service.pageProjectCenter(new ProjectCenterQueryDTO(), 1, 10);

        assertEquals(1, result.getRecords().size());
        ProjectCenterListVO item = result.getRecords().get(0);
        assertNotNull(item.getRecommendedNextStep());
        assertEquals("urgent", item.getRecommendedPriority());
        assertEquals("需求分析 / 产品经理", item.getRecommendedActorRole());
    }

    @Test
    void getProjectCenterDetailReturnsNextStepGuidance() {
        ProjectServiceImpl service = spy(newService());

        Project project = new Project();
        project.setId("p1");
        project.setProjectName("AICoOS 项目");
        project.setProjectCode("AIC-001");
        project.setProjectType("delivery");
        project.setCurrentStageCode("clarification");
        project.setOwnerUserId("u1");
        doReturn(project).when(service).getById("p1");

        ProjectGovernanceState state = new ProjectGovernanceState();
        state.setProjectId("p1");
        state.setCurrentStageCode("clarification");
        state.setCurrentStageName("需求澄清");
        state.setBlockedFlag(1);
        state.setAtRiskFlag(0);
        state.setBlockerClarificationCount(1);
        state.setGovernanceStatus("blocked");
        when(projectGovernanceLinkageService.getOrRecomputeProjectState("p1")).thenReturn(state);

        RequirementIntake intake = new RequirementIntake();
        intake.setProjectId("p1");
        intake.setBusinessGoal("Build governed delivery platform");
        when(requirementIntakeService.getByProjectId("p1")).thenReturn(intake);

        ClarificationItem blockerClarification = new ClarificationItem();
        blockerClarification.setProjectId("p1");
        blockerClarification.setTitle("确认首期范围");
        blockerClarification.setSeverity("blocker");
        blockerClarification.setStatus("open");
        when(clarificationItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(blockerClarification));

        when(projectStageService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(stage("clarification", "需求澄清")));
        when(decisionItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(approvalRecordService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(budgetPlanService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(budgetLedgerService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(meetingRecordService.listByProjectId("p1")).thenReturn(List.of());
        when(projectToolBindingService.listByProjectId("p1")).thenReturn(List.of());
        when(toolIntegrationAuditService.listByProjectId(eq("p1"), anyInt())).thenReturn(List.of());
        when(agentRoleService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        User owner = new User();
        owner.setId("u1");
        owner.setRealname("Admin");
        owner.setUsername("admin");
        when(userService.getById("u1")).thenReturn(owner);

        ProjectVO projectVO = new ProjectVO();
        projectVO.setId("p1");
        projectVO.setProjectName("AICoOS 项目");
        when(projectConvert.toVO(project)).thenReturn(projectVO);
        RequirementIntakeVO intakeVO = new RequirementIntakeVO();
        intakeVO.setProjectId("p1");
        when(requirementIntakeConvert.toVO(intake)).thenReturn(intakeVO);
        when(projectStageConvert.toVOs(any())).thenReturn(List.of(new ProjectStageVO()));
        when(clarificationItemConvert.toVOs(any())).thenReturn(List.of());
        when(decisionItemConvert.toVOs(any())).thenReturn(List.of());
        when(approvalRecordConvert.toVOs(any())).thenReturn(List.of());
        when(meetingRecordConvert.toVOs(any())).thenReturn(List.of());
        when(projectToolBindingConvert.toVOs(any())).thenReturn(List.of());
        when(toolIntegrationAuditConvert.toVOs(any())).thenReturn(List.of());

        ProjectCenterDetailVO detail = service.getProjectCenterDetail("p1");

        assertNotNull(detail.getNextStepGuidance());
        assertEquals("clarification-center", detail.getNextStepGuidance().getRecommendedTargetModule());
        assertEquals("urgent", detail.getNextStepGuidance().getRecommendedPriority());
        assertNotNull(detail.getNextStepGuidance().getPriorityActions());
        assertEquals("在澄清中心优先关闭阻塞澄清项", detail.getNextStepGuidance().getPriorityActions().get(0).getActionLabel());
    }

    private ProjectServiceImpl newService() {
        return new ProjectServiceImpl(
                projectConvert,
                projectStageConvert,
                requirementIntakeConvert,
                clarificationItemConvert,
                decisionItemConvert,
                meetingRecordConvert,
                approvalRecordConvert,
                budgetLedgerConvert,
                projectToolBindingConvert,
                toolIntegrationAuditConvert,
                projectStageService,
                requirementIntakeService,
                clarificationItemService,
                decisionItemService,
                meetingRecordService,
                approvalRecordService,
                projectToolBindingService,
                toolIntegrationAuditService,
                agentRoleService,
                agentRoleStageParticipationService,
                agentRoleAllowedActionService,
                budgetPlanService,
                budgetLedgerService,
                workflowTemplateService,
                workflowTemplateStageService,
                projectGovernanceLinkageService,
                userService
        );
    }

    private ProjectStage stage(String stageCode, String stageName) {
        ProjectStage stage = new ProjectStage();
        stage.setStageCode(stageCode);
        stage.setStageName(stageName);
        stage.setStageStatus("active");
        stage.setGateStatus("pending");
        stage.setStageOrder(1);
        return stage;
    }
}
