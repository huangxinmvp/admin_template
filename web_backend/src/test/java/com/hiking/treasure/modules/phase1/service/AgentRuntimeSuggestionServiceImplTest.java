package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import com.hiking.treasure.modules.phase1.mapper.ProjectMapper;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimeClient;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads;
import com.hiking.treasure.modules.phase1.service.impl.AgentRuntimeSuggestionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentRuntimeSuggestionServiceImplTest {

    @Mock
    private AgentRuntimeClient agentRuntimeClient;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private RequirementIntakeService requirementIntakeService;
    @Mock
    private ClarificationItemService clarificationItemService;
    @Mock
    private DecisionItemService decisionItemService;
    @Mock
    private ProjectStageService projectStageService;
    @Mock
    private ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Test
    void generateClarificationSuggestionsDoesNotPersistClarificationItems() {
        Project project = new Project();
        project.setId("p1");
        project.setProjectName("AICoOS");
        project.setProjectType("delivery");
        project.setCurrentStageCode("clarification");
        when(projectMapper.selectById("p1")).thenReturn(project);

        RequirementIntake intake = new RequirementIntake();
        intake.setProjectId("p1");
        intake.setBusinessGoal("Build governed AI delivery");
        when(requirementIntakeService.getByProjectId("p1")).thenReturn(intake);

        AgentRuntimePayloads.ClarificationSuggestion suggestion = new AgentRuntimePayloads.ClarificationSuggestion();
        suggestion.setTitle("明确业务目标边界");
        suggestion.setQuestion("核心业务目标是什么？");
        suggestion.setCategory("business_goal");
        suggestion.setSeverity("blocker");
        suggestion.setSuggestedOptions("提升转化率 / 降本");
        suggestion.setBlockerFlag(true);
        suggestion.setReason("没有目标就无法继续");
        AgentRuntimePayloads.ClarificationGenerateResponse response = new AgentRuntimePayloads.ClarificationGenerateResponse();
        response.setProvider("mock");
        response.setModel("mock-suggestion-v1");
        response.setSuggestions(List.of(suggestion));
        when(agentRuntimeClient.generateClarificationSuggestions(any())).thenReturn(response);

        AgentRuntimeSuggestionServiceImpl service = new AgentRuntimeSuggestionServiceImpl(
                agentRuntimeClient,
                projectMapper,
                requirementIntakeService,
                clarificationItemService,
                decisionItemService,
                projectStageService,
                projectGovernanceLinkageService
        );

        var result = service.generateClarificationSuggestions("p1");

        assertEquals(1, result.size());
        assertEquals("明确业务目标边界", result.get(0).getTitle());
        verify(clarificationItemService, never()).save(any());
    }

    @Test
    void generateDecisionSuggestionsDoesNotPromoteClarifications() {
        Project project = new Project();
        project.setId("p1");
        project.setProjectName("AICoOS");
        project.setProjectType("delivery");
        when(projectMapper.selectById("p1")).thenReturn(project);
        when(requirementIntakeService.getByProjectId("p1")).thenReturn(null);
        when(clarificationItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.<ClarificationItem>of());

        AgentRuntimePayloads.DecisionPromotionSuggestion suggestion =
                new AgentRuntimePayloads.DecisionPromotionSuggestion();
        suggestion.setClarificationId("c1");
        suggestion.setSuggestedTitle("确认首期范围");
        suggestion.setType("scope_change");
        suggestion.setImpactSummary("影响排期和成本");
        suggestion.setSuggestedOptions("先锁 MVP");
        suggestion.setRecommendedOption("先锁 MVP");
        suggestion.setBlockerFlag(true);
        suggestion.setReason("范围未锁定");
        AgentRuntimePayloads.DecisionPromotionSuggestionResponse response =
                new AgentRuntimePayloads.DecisionPromotionSuggestionResponse();
        response.setProvider("mock");
        response.setModel("mock-suggestion-v1");
        response.setSuggestions(List.of(suggestion));
        when(agentRuntimeClient.generateDecisionPromotionSuggestions(any())).thenReturn(response);

        AgentRuntimeSuggestionServiceImpl service = new AgentRuntimeSuggestionServiceImpl(
                agentRuntimeClient,
                projectMapper,
                requirementIntakeService,
                clarificationItemService,
                decisionItemService,
                projectStageService,
                projectGovernanceLinkageService
        );

        var result = service.generateDecisionSuggestions("p1");

        assertEquals(1, result.size());
        assertEquals("确认首期范围", result.get(0).getSuggestedTitle());
        verify(decisionItemService, never()).promoteFromClarification(any());
    }

    @Test
    void generateDecisionBudgetReviewDoesNotPersistDecisionChanges() {
        Project project = new Project();
        project.setId("p1");
        project.setProjectName("AICoOS");
        project.setProjectType("delivery");
        project.setCurrentStageCode("clarification");
        when(projectMapper.selectById("p1")).thenReturn(project);

        DecisionItem decisionItem = new DecisionItem();
        decisionItem.setId("d1");
        decisionItem.setProjectId("p1");
        decisionItem.setTitle("确认首期范围");
        decisionItem.setItemType("scope_change");
        when(decisionItemService.getById("d1")).thenReturn(decisionItem);

        AgentRuntimePayloads.DecisionBudgetReviewSuggestion suggestion =
                new AgentRuntimePayloads.DecisionBudgetReviewSuggestion();
        suggestion.setDecisionRecommendation("建议优先锁定 MVP 范围");
        suggestion.setBudgetImpactNote("建议预留预算缓冲");
        suggestion.setBudgetConfirmationAdvised(true);
        suggestion.setRecommendedOption("优先采用 MVP 方案");
        suggestion.setProjectImpactNote("可以降低返工风险");
        suggestion.setBlockerAssessment("若不确认预算会增加风险");
        suggestion.setNextSteps(List.of("补齐预算确认"));
        AgentRuntimePayloads.DecisionBudgetReviewResponse response =
                new AgentRuntimePayloads.DecisionBudgetReviewResponse();
        response.setProvider("mock");
        response.setModel("mock-suggestion-v1");
        response.setParticipants(List.of("Product Manager", "Budget Analyst"));
        response.setSuggestion(suggestion);
        when(agentRuntimeClient.generateDecisionBudgetReview(any())).thenReturn(response);

        AgentRuntimeSuggestionServiceImpl service = new AgentRuntimeSuggestionServiceImpl(
                agentRuntimeClient,
                projectMapper,
                requirementIntakeService,
                clarificationItemService,
                decisionItemService,
                projectStageService,
                projectGovernanceLinkageService
        );

        var result = service.generateDecisionBudgetReview("d1");

        assertEquals("建议优先锁定 MVP 范围", result.getDecisionRecommendation());
        assertEquals(1, result.getBudgetConfirmationAdvised());
        verify(decisionItemService, never()).updateById(any());
    }
}
