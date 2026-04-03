package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.domain.dto.command.ApplyClarificationSuggestionsDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.ApplyDecisionSuggestionsDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.BudgetImpactSuggestionApplyDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.DecisionBudgetReviewApplyDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.MeetingSummaryGenerateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.ProductArchitectureBriefGenerateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetImpactSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.ClarificationSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionBudgetReviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionPromotionSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.MeetingDecisionCandidateVO;
import com.hiking.treasure.modules.phase1.domain.vo.MeetingSummarySuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProductArchitectureBriefVO;
import com.hiking.treasure.modules.phase1.domain.vo.RequirementClarificationReviewVO;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.entity.ProjectGovernanceState;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationSeverity;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionItemStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionPriority;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionSourceType;
import com.hiking.treasure.modules.phase1.mapper.ProjectMapper;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimeClient;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads;
import com.hiking.treasure.modules.phase1.service.AgentRuntimeSuggestionService;
import com.hiking.treasure.modules.phase1.service.ClarificationItemService;
import com.hiking.treasure.modules.phase1.service.DecisionItemService;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.ProjectStageService;
import com.hiking.treasure.modules.phase1.service.RequirementIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentRuntimeSuggestionServiceImpl implements AgentRuntimeSuggestionService {

    private final AgentRuntimeClient agentRuntimeClient;
    private final ProjectMapper projectMapper;
    private final RequirementIntakeService requirementIntakeService;
    private final ClarificationItemService clarificationItemService;
    private final DecisionItemService decisionItemService;
    private final ProjectStageService projectStageService;
    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Override
    public List<ClarificationSuggestionVO> generateClarificationSuggestions(String projectId) {
        Project project = requireProject(projectId);
        RequirementIntake intake = requirementIntakeService.getByProjectId(projectId);
        AgentRuntimePayloads.ClarificationGenerateRequest request = new AgentRuntimePayloads.ClarificationGenerateRequest();
        request.setProjectName(firstNonBlank(
                intake == null ? null : intake.getProjectName(),
                project.getProjectName()));
        request.setProjectType(firstNonBlank(
                intake == null ? null : intake.getProjectType(),
                project.getProjectType()));
        request.setBusinessGoal(intake == null ? null : intake.getBusinessGoal());
        request.setFeatureSummary(intake == null ? null : intake.getFeatureSummary());
        request.setReferenceProducts(intake == null ? null : intake.getReferenceProducts());
        request.setTimelineExpectation(intake == null ? null : intake.getTimelineExpectation());
        request.setBudgetRange(intake == null ? null : intake.getBudgetRange());
        request.setTechnicalConstraints(intake == null ? null : intake.getTechnicalConstraints());
        request.setNotes(intake == null ? null : intake.getNotes());
        request.setMeta(defaultMeta());
        AgentRuntimePayloads.ClarificationGenerateResponse response =
                agentRuntimeClient.generateClarificationSuggestions(request);
        return safe(response.getSuggestions()).stream()
                .map(this::toClarificationSuggestionVO)
                .toList();
    }

    @Override
    public RequirementClarificationReviewVO generateRequirementClarificationReview(String projectId) {
        Project project = requireProject(projectId);
        RequirementIntake intake = requirementIntakeService.getByProjectId(projectId);
        List<ClarificationItem> existingClarifications = clarificationItemService.list(new LambdaQueryWrapper<ClarificationItem>()
                .eq(ClarificationItem::getProjectId, projectId));
        int existingClarificationCount = existingClarifications.size();
        long openClarificationCount = existingClarifications.stream()
                .filter(item -> !Objects.equals(item.getStatus(), ClarificationStatus.RESOLVED.getCode()))
                .count();
        long blockerClarificationCount = existingClarifications.stream()
                .filter(item -> !Objects.equals(item.getStatus(), ClarificationStatus.RESOLVED.getCode()))
                .filter(item -> Objects.equals(item.getSeverity(), ClarificationSeverity.BLOCKER.getCode()))
                .count();
        ProjectGovernanceState governanceState = projectGovernanceLinkageService.getOrRecomputeProjectState(projectId);
        AgentRuntimePayloads.RequirementClarificationReviewRequest request =
                new AgentRuntimePayloads.RequirementClarificationReviewRequest();
        request.setProjectName(firstNonBlank(
                intake == null ? null : intake.getProjectName(),
                project.getProjectName()));
        request.setProjectType(firstNonBlank(
                intake == null ? null : intake.getProjectType(),
                project.getProjectType()));
        request.setCurrentStageCode(resolveCurrentStageCode(projectId, project.getCurrentStageCode()));
        request.setBusinessGoal(intake == null ? null : intake.getBusinessGoal());
        request.setFeatureSummary(intake == null ? null : intake.getFeatureSummary());
        request.setReferenceProducts(intake == null ? null : intake.getReferenceProducts());
        request.setTimelineExpectation(intake == null ? null : intake.getTimelineExpectation());
        request.setBudgetRange(intake == null ? null : intake.getBudgetRange());
        request.setTechnicalConstraints(intake == null ? null : intake.getTechnicalConstraints());
        request.setNotes(intake == null ? null : intake.getNotes());
        request.setExistingClarificationCount(existingClarificationCount);
        request.setMeta(defaultMeta());
        AgentRuntimePayloads.RequirementClarificationReviewResponse response =
                agentRuntimeClient.generateRequirementClarificationReview(request);
        LinkedHashMap<String, Object> requirementLinkageExtras = new LinkedHashMap<>();
        requirementLinkageExtras.put("existingClarificationCount", existingClarificationCount);
        requirementLinkageExtras.put("openClarificationCount", openClarificationCount);
        requirementLinkageExtras.put("existingBlockerClarificationCount", blockerClarificationCount);
        requirementLinkageExtras.put("blockerSuggestionCount", safe(response.getSuggestions()).stream()
                .filter(item -> Boolean.TRUE.equals(item.getBlockerFlag()))
                .count());
        requirementLinkageExtras.put("decisionEscalationSuggestionCount", safe(response.getSuggestions()).stream()
                .filter(item -> Boolean.TRUE.equals(item.getEscalationRecommended()))
                .count());
        requirementLinkageExtras.put("followUpModules", safe(response.getSuggestions()).stream()
                .map(AgentRuntimePayloads.ClarificationSuggestion::getFollowUpModule)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList());
        if (governanceState != null) {
            requirementLinkageExtras.put("projectGovernanceStatus", governanceState.getGovernanceStatus());
            requirementLinkageExtras.put("blockerReasonSummary", governanceState.getBlockerReasonSummary());
            requirementLinkageExtras.put("pendingDecisionItemCount", governanceState.getPendingDecisionCount());
            requirementLinkageExtras.put("pendingApprovalCount", governanceState.getPendingApprovalCount());
        }
        RequirementClarificationReviewVO vo = new RequirementClarificationReviewVO();
        vo.setParticipants(safe(response.getParticipants()));
        vo.setScenarioLabel(response.getScenarioLabel());
        vo.setCollaborationSummary(response.getCollaborationSummary());
        vo.setRecommendedOperatorAction(response.getRecommendedOperatorAction());
        vo.setGovernanceLinkage(withGovernanceLinkage(
                response.getGovernanceLinkage(),
                "clarification_center",
                projectId,
                resolveCurrentStageCode(projectId, project.getCurrentStageCode()),
                requirementLinkageExtras));
        vo.setRoleInsights(safe(response.getRoleInsights()));
        vo.setSelectionGuidance(response.getSelectionGuidance());
        vo.setGovernanceInterpretation(response.getGovernanceInterpretation());
        vo.setDecisionEscalationAdvised(booleanToInt(response.getDecisionEscalationAdvised()));
        vo.setFollowUpHints(safe(response.getFollowUpHints()));
        vo.setBlockerAssessment(response.getBlockerAssessment());
        vo.setNextQuestions(safe(response.getNextQuestions()));
        vo.setSuggestions(safe(response.getSuggestions()).stream()
                .map(this::toClarificationSuggestionVO)
                .toList());
        return normalizeRequirementClarificationReviewVO(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ClarificationItem> applyClarificationSuggestions(
            String projectId,
            ApplyClarificationSuggestionsDTO dto) {
        requireProject(projectId);
        List<ApplyClarificationSuggestionsDTO.SuggestionItem> suggestions = dto == null ? List.of() : safe(dto.getSuggestions());
        if (suggestions.isEmpty()) {
            throw new BusinessException(400, "请选择至少一条澄清建议");
        }
        Set<String> existingTitles = clarificationItemService.list(new LambdaQueryWrapper<ClarificationItem>()
                        .eq(ClarificationItem::getProjectId, projectId))
                .stream()
                .map(ClarificationItem::getTitle)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<ClarificationItem> created = new ArrayList<>();
        for (ApplyClarificationSuggestionsDTO.SuggestionItem suggestion : suggestions) {
            if (suggestion == null || isBlank(suggestion.getQuestion())) {
                continue;
            }
            if (!isBlank(suggestion.getTitle()) && existingTitles.contains(suggestion.getTitle())) {
                continue;
            }
            ClarificationItem item = new ClarificationItem();
            item.setProjectId(projectId);
            item.setTitle(firstNonBlank(suggestion.getTitle(), suggestion.getQuestion(), "AI 澄清建议"));
            item.setQuestion(suggestion.getQuestion());
            item.setCategory(suggestion.getCategory());
            item.setSeverity(firstNonBlank(suggestion.getSeverity(), "medium"));
            item.setSuggestedOptions(suggestion.getSuggestedOptions());
            item.setStatus(ClarificationStatus.OPEN.getCode());
            item.setGeneratedFlag(1);
            item.setRemark(joinRemark(
                    "requirement_clarification_collaboration".equals(dto == null ? null : dto.getSourceContext())
                            ? "来源：多角色澄清评审"
                            : null,
                    suggestion.getReason(),
                    suggestion.getGovernanceReason(),
                    suggestion.getEscalationRecommended() != null && suggestion.getEscalationRecommended() > 0
                            ? "建议人工判断后提升至决策中心"
                            : null,
                    suggestion.getFollowUpModule() == null ? null : "建议跟进模块：" + suggestion.getFollowUpModule()));
            clarificationItemService.save(item);
            created.add(clarificationItemService.getById(item.getId()));
        }
        return created;
    }

    @Override
    public List<DecisionPromotionSuggestionVO> generateDecisionSuggestions(String projectId) {
        Project project = requireProject(projectId);
        List<ClarificationItem> clarificationItems = clarificationItemService.list(new LambdaQueryWrapper<ClarificationItem>()
                .eq(ClarificationItem::getProjectId, projectId)
                .isNull(ClarificationItem::getPromotedDecisionItemId)
                .orderByDesc(ClarificationItem::getUpdateTime)
                .orderByDesc(ClarificationItem::getCreateTime));
        RequirementIntake intake = requirementIntakeService.getByProjectId(projectId);
        AgentRuntimePayloads.DecisionPromotionSuggestionRequest request =
                new AgentRuntimePayloads.DecisionPromotionSuggestionRequest();
        request.setProjectName(project.getProjectName());
        request.setProjectType(project.getProjectType());
        request.setCurrentStageCode(project.getCurrentStageCode());
        request.setRequirementSummary(firstNonBlank(
                project.getIntakeSummary(),
                intake == null ? null : intake.getFeatureSummary(),
                intake == null ? null : intake.getBusinessGoal()));
        request.setClarifications(clarificationItems.stream().map(item -> {
            AgentRuntimePayloads.ClarificationContext context = new AgentRuntimePayloads.ClarificationContext();
            context.setClarificationId(item.getId());
            context.setTitle(item.getTitle());
            context.setQuestion(item.getQuestion());
            context.setCategory(item.getCategory());
            context.setSeverity(item.getSeverity());
            context.setSuggestedOptions(item.getSuggestedOptions());
            context.setUserResponse(item.getUserResponse());
            context.setStatus(item.getStatus());
            return context;
        }).toList());
        request.setMeta(defaultMeta());
        AgentRuntimePayloads.DecisionPromotionSuggestionResponse response =
                agentRuntimeClient.generateDecisionPromotionSuggestions(request);
        return safe(response.getSuggestions()).stream()
                .map(this::toDecisionSuggestionVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DecisionItem> applyDecisionSuggestions(String projectId, ApplyDecisionSuggestionsDTO dto) {
        requireProject(projectId);
        List<ApplyDecisionSuggestionsDTO.SuggestionItem> suggestions = dto == null ? List.of() : safe(dto.getSuggestions());
        if (suggestions.isEmpty()) {
            throw new BusinessException(400, "请选择至少一条决策建议");
        }
        List<DecisionItem> results = new ArrayList<>();
        for (ApplyDecisionSuggestionsDTO.SuggestionItem suggestion : suggestions) {
            if (suggestion == null || isBlank(suggestion.getClarificationId())) {
                continue;
            }
            DecisionItem decisionItem = decisionItemService.promoteFromClarification(suggestion.getClarificationId());
            DecisionItem update = new DecisionItem();
            update.setId(decisionItem.getId());
            update.setProjectId(projectId);
            update.setTitle(firstNonBlank(suggestion.getSuggestedTitle(), decisionItem.getTitle()));
            update.setItemType(firstNonBlank(suggestion.getType(), decisionItem.getItemType()));
            update.setSourceType(DecisionSourceType.CLARIFICATION.getCode());
            update.setDescription(firstNonBlank(decisionItem.getDescription(), decisionItem.getTitle()));
            update.setImpactSummary(firstNonBlank(suggestion.getImpactSummary(), decisionItem.getImpactSummary()));
            update.setSuggestedOptions(firstNonBlank(suggestion.getSuggestedOptions(), decisionItem.getSuggestedOptions()));
            update.setRecommendedOption(firstNonBlank(suggestion.getRecommendedOption(), decisionItem.getRecommendedOption()));
            update.setBlockerFlag(booleanToInt(Boolean.TRUE.equals(toBoolean(suggestion.getBlockerFlag()))));
            update.setPriority(Boolean.TRUE.equals(toBoolean(suggestion.getBlockerFlag()))
                    ? DecisionPriority.CRITICAL.getCode()
                    : decisionItem.getPriority());
            update.setRemark(joinRemark("由 AI 建议提升为正式决策事项", suggestion.getReason()));
            decisionItemService.updateById(update);
            results.add(decisionItemService.getById(decisionItem.getId()));
        }
        return results;
    }

    @Override
    public BudgetImpactSuggestionVO generateDecisionBudgetImpactSuggestion(String decisionItemId) {
        DecisionItem decisionItem = requireDecision(decisionItemId);
        Project project = requireProject(decisionItem.getProjectId());
        RequirementIntake intake = requirementIntakeService.getByProjectId(project.getId());
        AgentRuntimePayloads.BudgetImpactSuggestionRequest request =
                new AgentRuntimePayloads.BudgetImpactSuggestionRequest();
        request.setProjectName(project.getProjectName());
        request.setProjectType(project.getProjectType());
        request.setCurrentStageCode(resolveCurrentStageCode(project.getId(), project.getCurrentStageCode()));
        request.setBudgetRange(intake == null ? null : intake.getBudgetRange());
        request.setDecisionTitle(decisionItem.getTitle());
        request.setDecisionType(decisionItem.getItemType());
        request.setDecisionDescription(decisionItem.getDescription());
        request.setDecisionImpactSummary(decisionItem.getImpactSummary());
        request.setRequirementSummary(firstNonBlank(project.getIntakeSummary(), intake == null ? null : intake.getFeatureSummary()));
        request.setMeta(defaultMeta());
        AgentRuntimePayloads.BudgetImpactSuggestionResponse response =
                agentRuntimeClient.generateBudgetImpactSuggestion(request);
        return toBudgetSuggestionVO(response == null ? null : response.getSuggestion());
    }

    @Override
    public DecisionBudgetReviewVO generateDecisionBudgetReview(String decisionItemId) {
        DecisionItem decisionItem = requireDecision(decisionItemId);
        Project project = requireProject(decisionItem.getProjectId());
        RequirementIntake intake = requirementIntakeService.getByProjectId(project.getId());
        ProjectGovernanceState governanceState = projectGovernanceLinkageService.getOrRecomputeProjectState(project.getId());
        AgentRuntimePayloads.DecisionBudgetReviewRequest request =
                new AgentRuntimePayloads.DecisionBudgetReviewRequest();
        request.setProjectName(project.getProjectName());
        request.setProjectType(project.getProjectType());
        request.setCurrentStageCode(resolveCurrentStageCode(project.getId(), project.getCurrentStageCode()));
        request.setBudgetRange(intake == null ? null : intake.getBudgetRange());
        request.setDecisionTitle(decisionItem.getTitle());
        request.setDecisionType(decisionItem.getItemType());
        request.setDecisionDescription(decisionItem.getDescription());
        request.setDecisionImpactSummary(decisionItem.getImpactSummary());
        request.setSuggestedOptions(decisionItem.getSuggestedOptions());
        request.setRecommendedOption(decisionItem.getRecommendedOption());
        request.setBlockerFlag(Boolean.TRUE.equals(toBoolean(decisionItem.getBlockerFlag())));
        request.setBudgetImpactSummary(decisionItem.getBudgetImpactSummary());
        request.setProjectImpactSummary(decisionItem.getProjectImpactSummary());
        request.setRequirementSummary(firstNonBlank(
                project.getIntakeSummary(),
                intake == null ? null : intake.getFeatureSummary(),
                intake == null ? null : intake.getBusinessGoal()));
        request.setMeta(defaultMeta());
        AgentRuntimePayloads.DecisionBudgetReviewResponse response =
                agentRuntimeClient.generateDecisionBudgetReview(request);
        DecisionBudgetReviewVO vo = toDecisionBudgetReviewVO(response);
        LinkedHashMap<String, Object> decisionBudgetLinkageExtras = new LinkedHashMap<>();
        decisionBudgetLinkageExtras.put("decisionId", decisionItemId);
        decisionBudgetLinkageExtras.put("currentDecisionStatus", decisionItem.getStatus());
        decisionBudgetLinkageExtras.put("currentDecisionPriority", decisionItem.getPriority());
        decisionBudgetLinkageExtras.put("currentDecisionBlockerFlag", decisionItem.getBlockerFlag());
        decisionBudgetLinkageExtras.put(
                "requiresBudgetConfirmation",
                Boolean.TRUE.equals(toBoolean(vo.getBudgetConfirmationAdvised())));
        decisionBudgetLinkageExtras.put(
                "shouldOpenApproval",
                decisionItem.getBlockerFlag() != null && decisionItem.getBlockerFlag() > 0);
        if (governanceState != null) {
            decisionBudgetLinkageExtras.put("projectGovernanceStatus", governanceState.getGovernanceStatus());
            decisionBudgetLinkageExtras.put("currentBudgetStatus", governanceState.getBudgetStatus());
            decisionBudgetLinkageExtras.put("pendingApprovalCount", governanceState.getPendingApprovalCount());
            decisionBudgetLinkageExtras.put("blockerApprovalCount", governanceState.getBlockerApprovalCount());
            decisionBudgetLinkageExtras.put("pendingDecisionItemCount", governanceState.getPendingDecisionCount());
        }
        vo.setGovernanceLinkage(withGovernanceLinkage(
                vo.getGovernanceLinkage(),
                "decision_center",
                project.getId(),
                resolveCurrentStageCode(project.getId(), project.getCurrentStageCode()),
                decisionBudgetLinkageExtras));
        return normalizeDecisionBudgetReviewVO(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DecisionItem applyDecisionBudgetImpactSuggestion(
            String decisionItemId,
            BudgetImpactSuggestionApplyDTO dto) {
        DecisionItem decisionItem = requireDecision(decisionItemId);
        DecisionItem update = new DecisionItem();
        update.setId(decisionItemId);
        update.setProjectId(decisionItem.getProjectId());
        update.setStatus(firstNonBlank(decisionItem.getStatus(), DecisionItemStatus.OPEN.getCode()));
        update.setBudgetImpactSummary(dto == null ? null : dto.getBudgetImpactSummary());
        update.setProjectImpactSummary(dto == null ? null : dto.getProjectImpactSummary());
        update.setRemark(joinRemark(
                "已应用 AI 预算影响建议",
                dto == null ? null : "预算变化范围：" + dto.getDeltaRange(),
                dto == null || dto.getAffectedRoles() == null || dto.getAffectedRoles().isEmpty()
                        ? null
                        : "受影响角色：" + String.join(" / ", dto.getAffectedRoles()),
                dto == null ? null : "可信度：" + dto.getConfidence()));
        decisionItemService.updateById(update);
        return decisionItemService.getById(decisionItemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DecisionItem applyDecisionBudgetReview(String decisionItemId, DecisionBudgetReviewApplyDTO dto) {
        DecisionItem decisionItem = requireDecision(decisionItemId);
        DecisionItem update = new DecisionItem();
        update.setId(decisionItemId);
        update.setProjectId(decisionItem.getProjectId());
        update.setStatus(firstNonBlank(decisionItem.getStatus(), DecisionItemStatus.OPEN.getCode()));
        update.setImpactSummary(firstNonBlank(
                dto == null ? null : dto.getDecisionRecommendation(),
                decisionItem.getImpactSummary()));
        update.setRecommendedOption(firstNonBlank(
                dto == null ? null : dto.getRecommendedOption(),
                decisionItem.getRecommendedOption()));
        update.setBudgetImpactSummary(firstNonBlank(
                dto == null ? null : dto.getBudgetImpactNote(),
                decisionItem.getBudgetImpactSummary()));
        update.setProjectImpactSummary(firstNonBlank(
                dto == null ? null : dto.getProjectImpactNote(),
                decisionItem.getProjectImpactSummary()));
        update.setRemark(joinRemark(
                "已应用多角色协作评审建议",
                dto == null ? null : dto.getBlockerAssessment(),
                dto == null ? null : dto.getDecisionLinkageSummary(),
                dto == null ? null : dto.getBudgetLinkageSummary(),
                dto != null && Boolean.TRUE.equals(toBoolean(dto.getBudgetConfirmationAdvised()))
                        ? "建议补充预算确认"
                        : null,
                dto == null || safe(dto.getNextSteps()).isEmpty()
                        ? null
                        : "下一步：" + String.join(" / ", safe(dto.getNextSteps())),
                dto == null || safe(dto.getFollowUpHints()).isEmpty()
                        ? null
                        : "治理跟进：" + String.join(" / ", safe(dto.getFollowUpHints()))));
        decisionItemService.updateById(update);
        return decisionItemService.getById(decisionItemId);
    }

    @Override
    public ProductArchitectureBriefVO generateProductArchitectureBrief(
            String projectId,
            ProductArchitectureBriefGenerateDTO dto) {
        Project project = requireProject(projectId);
        RequirementIntake intake = requirementIntakeService.getByProjectId(projectId);
        ProjectGovernanceState governanceState = projectGovernanceLinkageService.getOrRecomputeProjectState(projectId);
        AgentRuntimePayloads.ProductArchitectureBriefRequest request =
                new AgentRuntimePayloads.ProductArchitectureBriefRequest();
        request.setProjectName(project.getProjectName());
        request.setProjectType(project.getProjectType());
        request.setCurrentStageCode(resolveCurrentStageCode(projectId, project.getCurrentStageCode()));
        request.setBusinessGoal(intake == null ? null : intake.getBusinessGoal());
        request.setFeatureSummary(intake == null ? null : intake.getFeatureSummary());
        request.setTechnicalConstraints(intake == null ? null : intake.getTechnicalConstraints());
        request.setRequirementSummary(firstNonBlank(
                project.getIntakeSummary(),
                intake == null ? null : intake.getFeatureSummary(),
                intake == null ? null : intake.getBusinessGoal()));
        request.setFocusNotes(dto == null ? null : dto.getFocusNotes());
        request.setMeta(defaultMeta());
        AgentRuntimePayloads.ProductArchitectureBriefResponse response =
                agentRuntimeClient.generateProductArchitectureBrief(request);
        ProductArchitectureBriefVO vo = toProductArchitectureBriefVO(response);
        LinkedHashMap<String, Object> productArchitectureLinkageExtras = new LinkedHashMap<>();
        productArchitectureLinkageExtras.put("recommendedArtifactType", "meeting_record");
        if (governanceState != null) {
            productArchitectureLinkageExtras.put("projectGovernanceStatus", governanceState.getGovernanceStatus());
            productArchitectureLinkageExtras.put("blockedFlag", governanceState.getBlockedFlag());
            productArchitectureLinkageExtras.put("atRiskFlag", governanceState.getAtRiskFlag());
            productArchitectureLinkageExtras.put("blockerReasonSummary", governanceState.getBlockerReasonSummary());
            productArchitectureLinkageExtras.put("pendingDecisionCount", governanceState.getPendingDecisionCount());
            productArchitectureLinkageExtras.put("pendingApprovalCount", governanceState.getPendingApprovalCount());
            productArchitectureLinkageExtras.put("failedGateConditionCount", governanceState.getFailedGateConditionCount());
            productArchitectureLinkageExtras.put("missingCriticalRoleCount", governanceState.getMissingCriticalRoleCount());
        }
        vo.setGovernanceLinkage(withGovernanceLinkage(
                vo.getGovernanceLinkage(),
                "project_center",
                projectId,
                resolveCurrentStageCode(projectId, project.getCurrentStageCode()),
                productArchitectureLinkageExtras));
        return normalizeProductArchitectureBriefVO(vo);
    }

    @Override
    public MeetingSummarySuggestionVO generateMeetingSummary(String projectId, MeetingSummaryGenerateDTO dto) {
        Project project = requireProject(projectId);
        if (dto == null || isBlank(dto.getRawNotes())) {
            throw new BusinessException(400, "会议原始笔记不能为空");
        }
        AgentRuntimePayloads.MeetingSummaryRequest request = new AgentRuntimePayloads.MeetingSummaryRequest();
        request.setProjectName(project.getProjectName());
        request.setCurrentStageCode(resolveCurrentStageCode(projectId, project.getCurrentStageCode()));
        request.setMeetingTitle(dto.getMeetingTitle());
        request.setRawNotes(dto.getRawNotes());
        request.setSourceType(dto.getSourceType());
        request.setSourceObjectId(dto.getSourceObjectId());
        request.setMeta(defaultMeta());
        AgentRuntimePayloads.MeetingSummaryResponse response = agentRuntimeClient.generateMeetingSummary(request);
        return toMeetingSummarySuggestionVO(response == null ? null : response.getSuggestion());
    }

    private Project requireProject(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new BusinessException(400, "项目ID不能为空");
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        return project;
    }

    private DecisionItem requireDecision(String decisionItemId) {
        DecisionItem decisionItem = decisionItemService.getById(decisionItemId);
        if (decisionItem == null) {
            throw new BusinessException(404, "决策事项不存在");
        }
        return decisionItem;
    }

    private AgentRuntimePayloads.RequestMeta defaultMeta() {
        AgentRuntimePayloads.RequestMeta meta = new AgentRuntimePayloads.RequestMeta();
        return meta;
    }

    private ClarificationSuggestionVO toClarificationSuggestionVO(AgentRuntimePayloads.ClarificationSuggestion suggestion) {
        ClarificationSuggestionVO vo = new ClarificationSuggestionVO();
        vo.setTitle(suggestion.getTitle());
        vo.setQuestion(suggestion.getQuestion());
        vo.setCategory(suggestion.getCategory());
        vo.setSeverity(suggestion.getSeverity());
        vo.setSuggestedOptions(suggestion.getSuggestedOptions());
        vo.setBlockerFlag(booleanToInt(suggestion.getBlockerFlag()));
        vo.setReason(suggestion.getReason());
        vo.setGovernanceReason(suggestion.getGovernanceReason());
        vo.setFollowUpModule(suggestion.getFollowUpModule());
        vo.setEscalationRecommended(booleanToInt(suggestion.getEscalationRecommended()));
        return vo;
    }

    private DecisionPromotionSuggestionVO toDecisionSuggestionVO(
            AgentRuntimePayloads.DecisionPromotionSuggestion suggestion) {
        DecisionPromotionSuggestionVO vo = new DecisionPromotionSuggestionVO();
        vo.setClarificationId(suggestion.getClarificationId());
        vo.setSuggestedTitle(suggestion.getSuggestedTitle());
        vo.setType(suggestion.getType());
        vo.setImpactSummary(suggestion.getImpactSummary());
        vo.setSuggestedOptions(suggestion.getSuggestedOptions());
        vo.setRecommendedOption(suggestion.getRecommendedOption());
        vo.setBlockerFlag(booleanToInt(suggestion.getBlockerFlag()));
        vo.setReason(suggestion.getReason());
        return vo;
    }

    private BudgetImpactSuggestionVO toBudgetSuggestionVO(AgentRuntimePayloads.BudgetImpactSuggestion suggestion) {
        BudgetImpactSuggestionVO vo = new BudgetImpactSuggestionVO();
        if (suggestion == null) {
            return vo;
        }
        vo.setBudgetImpactSummary(suggestion.getBudgetImpactSummary());
        vo.setProjectImpactSummary(suggestion.getProjectImpactSummary());
        vo.setDeltaRange(suggestion.getDeltaRange());
        vo.setAffectedRoles(safe(suggestion.getAffectedRoles()));
        vo.setConfidence(suggestion.getConfidence());
        return vo;
    }

    private DecisionBudgetReviewVO toDecisionBudgetReviewVO(
            AgentRuntimePayloads.DecisionBudgetReviewResponse response) {
        DecisionBudgetReviewVO vo = new DecisionBudgetReviewVO();
        vo.setParticipants(safe(response == null ? null : response.getParticipants()));
        if (response == null) {
            return vo;
        }
        AgentRuntimePayloads.DecisionBudgetReviewSuggestion suggestion = response.getSuggestion();
        vo.setScenarioLabel(response.getScenarioLabel());
        vo.setCollaborationSummary(response.getCollaborationSummary());
        vo.setRecommendedOperatorAction(response.getRecommendedOperatorAction());
        vo.setGovernanceLinkage(response.getGovernanceLinkage());
        vo.setRoleInsights(response.getRoleInsights());
        vo.setRiskFlags(safe(response.getRiskFlags()));
        vo.setDecisionLinkageSummary(response.getDecisionLinkageSummary());
        vo.setBudgetLinkageSummary(response.getBudgetLinkageSummary());
        vo.setFollowUpHints(safe(response.getFollowUpHints()));
        if (suggestion == null) {
            return vo;
        }
        vo.setDecisionRecommendation(suggestion.getDecisionRecommendation());
        vo.setBudgetImpactNote(suggestion.getBudgetImpactNote());
        vo.setBudgetConfirmationAdvised(booleanToInt(suggestion.getBudgetConfirmationAdvised()));
        vo.setRecommendedOption(suggestion.getRecommendedOption());
        vo.setProjectImpactNote(suggestion.getProjectImpactNote());
        vo.setBlockerAssessment(suggestion.getBlockerAssessment());
        vo.setNextSteps(safe(suggestion.getNextSteps()));
        return vo;
    }

    private ProductArchitectureBriefVO toProductArchitectureBriefVO(
            AgentRuntimePayloads.ProductArchitectureBriefResponse response) {
        ProductArchitectureBriefVO vo = new ProductArchitectureBriefVO();
        vo.setParticipants(safe(response == null ? null : response.getParticipants()));
        if (response == null) {
            return vo;
        }
        AgentRuntimePayloads.ProductArchitectureBriefSuggestion suggestion = response.getSuggestion();
        vo.setScenarioLabel(response.getScenarioLabel());
        vo.setCollaborationSummary(response.getCollaborationSummary());
        vo.setRecommendedOperatorAction(response.getRecommendedOperatorAction());
        vo.setGovernanceLinkage(response.getGovernanceLinkage());
        vo.setRoleInsights(response.getRoleInsights());
        vo.setArchitectureFocusAreas(safe(response.getArchitectureFocusAreas()));
        vo.setDeliveryImplications(safe(response.getDeliveryImplications()));
        vo.setProjectGovernanceLinkageSummary(response.getProjectGovernanceLinkageSummary());
        vo.setFollowUpHints(safe(response.getFollowUpHints()));
        if (suggestion == null) {
            return vo;
        }
        vo.setBriefTitle(suggestion.getBriefTitle());
        vo.setSolutionBrief(suggestion.getSolutionBrief());
        vo.setRisks(safe(suggestion.getRisks()));
        vo.setOpenQuestions(safe(suggestion.getOpenQuestions()));
        vo.setNextSteps(safe(suggestion.getNextSteps()));
        return vo;
    }

    private MeetingSummarySuggestionVO toMeetingSummarySuggestionVO(
            AgentRuntimePayloads.MeetingSummarySuggestion suggestion) {
        MeetingSummarySuggestionVO vo = new MeetingSummarySuggestionVO();
        if (suggestion == null) {
            return vo;
        }
        vo.setMeetingTitle(suggestion.getMeetingTitle());
        vo.setSummary(suggestion.getSummary());
        vo.setActionItems(safe(suggestion.getActionItems()));
        vo.setOpenQuestions(safe(suggestion.getOpenQuestions()));
        vo.setDecisionCandidates(safe(suggestion.getDecisionCandidates()).stream().map(item -> {
            MeetingDecisionCandidateVO candidate = new MeetingDecisionCandidateVO();
            candidate.setTitle(item.getTitle());
            candidate.setDecisionType(item.getDecisionType());
            candidate.setBlockerFlag(booleanToInt(item.getBlockerFlag()));
            candidate.setRecommendedOption(item.getRecommendedOption());
            return candidate;
        }).toList());
        return vo;
    }

    private RequirementClarificationReviewVO normalizeRequirementClarificationReviewVO(
            RequirementClarificationReviewVO vo) {
        if (vo == null) {
            return null;
        }
        List<ClarificationSuggestionVO> suggestions = dedupeClarificationSuggestions(safe(vo.getSuggestions()));
        vo.setSuggestions(suggestions);
        if (vo.getDecisionEscalationAdvised() == null) {
            vo.setDecisionEscalationAdvised(
                    suggestions.stream().anyMatch(item -> zeroIfNull(item.getEscalationRecommended()) > 0) ? 1 : 0);
        }
        vo.setCollaborationSummary(cleanText(firstNonBlank(
                vo.getCollaborationSummary(),
                buildRequirementCollaborationSummary(suggestions))));
        vo.setRecommendedOperatorAction(cleanText(firstNonBlank(
                vo.getRecommendedOperatorAction(),
                buildRequirementOperatorAction(suggestions))));
        vo.setSelectionGuidance(cleanText(firstNonBlank(
                vo.getSelectionGuidance(),
                "先选阻塞项，再选会影响 MVP、预算或里程碑承诺的事项。")));
        vo.setGovernanceInterpretation(cleanText(firstNonBlank(
                vo.getGovernanceInterpretation(),
                "澄清中心负责补输入；只有改变范围、预算或时间承诺的事项才建议人工升级到决策中心。")));
        vo.setRoleInsights(limitDistinctStrings(
                ensureRequirementRoleInsights(safe(vo.getRoleInsights())),
                2,
                vo.getCollaborationSummary(),
                vo.getRecommendedOperatorAction(),
                vo.getSelectionGuidance(),
                vo.getGovernanceInterpretation()));
        vo.setFollowUpHints(limitDistinctStrings(
                safe(vo.getFollowUpHints()),
                3,
                vo.getCollaborationSummary(),
                vo.getRecommendedOperatorAction(),
                vo.getSelectionGuidance(),
                vo.getGovernanceInterpretation()));
        vo.setNextQuestions(limitDistinctStrings(
                excludeSimilarStrings(
                        safe(vo.getNextQuestions()),
                        suggestions.stream()
                                .map(ClarificationSuggestionVO::getQuestion)
                                .toList()),
                3));
        return vo;
    }

    private DecisionBudgetReviewVO normalizeDecisionBudgetReviewVO(DecisionBudgetReviewVO vo) {
        if (vo == null) {
            return null;
        }
        vo.setCollaborationSummary(cleanText(firstNonBlank(
                vo.getCollaborationSummary(),
                "本次协作重点判断当前决策是否直接留档、是否补预算确认，以及是否需要人工审批判断。")));
        vo.setRecommendedOperatorAction(cleanText(firstNonBlank(
                vo.getRecommendedOperatorAction(),
                buildDecisionBudgetOperatorAction(vo))));
        vo.setRiskFlags(limitDistinctStrings(safe(vo.getRiskFlags()), 3));
        vo.setDecisionLinkageSummary(cleanText(firstNonBlank(
                vo.getDecisionLinkageSummary(),
                "当前更适合沿用现有决策单继续推进，不建议再建平行确认项。")));
        vo.setBudgetLinkageSummary(cleanText(firstNonBlank(
                vo.getBudgetLinkageSummary(),
                zeroIfNull(vo.getBudgetConfirmationAdvised()) > 0
                        ? "建议先补预算确认说明，当前评审不会自动改预算。"
                        : "当前以预算留档为主，无需自动触发预算调整。")));
        vo.setFollowUpHints(limitDistinctStrings(
                safe(vo.getFollowUpHints()),
                3,
                vo.getCollaborationSummary(),
                vo.getRecommendedOperatorAction(),
                vo.getDecisionLinkageSummary(),
                vo.getBudgetLinkageSummary()));
        vo.setNextSteps(limitDistinctStrings(
                safe(vo.getNextSteps()),
                3,
                vo.getRecommendedOperatorAction(),
                vo.getDecisionLinkageSummary(),
                vo.getBudgetLinkageSummary()));
        vo.setDecisionRecommendation(cleanText(vo.getDecisionRecommendation()));
        vo.setBudgetImpactNote(cleanText(vo.getBudgetImpactNote()));
        vo.setProjectImpactNote(cleanText(vo.getProjectImpactNote()));
        vo.setBlockerAssessment(cleanText(vo.getBlockerAssessment()));
        vo.setRoleInsights(normalizeRoleInsightMap(vo.getRoleInsights(),
                Map.of(
                        "productManagerView", "Product Manager：先锁业务收益和首期边界，再决定是否推进。",
                        "budgetAnalystView", "Budget Analyst：先判断预算缓冲和角色投入是否需要额外确认。")));
        return vo;
    }

    private ProductArchitectureBriefVO normalizeProductArchitectureBriefVO(ProductArchitectureBriefVO vo) {
        if (vo == null) {
            return null;
        }
        vo.setCollaborationSummary(cleanText(firstNonBlank(
                vo.getCollaborationSummary(),
                "先锁首期方案边界，再把高风险点和开放问题送回现有治理模块继续收敛。")));
        vo.setRecommendedOperatorAction(cleanText(firstNonBlank(
                vo.getRecommendedOperatorAction(),
                buildProductArchitectureOperatorAction(vo))));
        vo.setProjectGovernanceLinkageSummary(cleanText(firstNonBlank(
                vo.getProjectGovernanceLinkageSummary(),
                "这份简报适合作为项目记录沉淀，并把高风险点拆回澄清中心、决策中心或后续设计评审。")));
        vo.setFollowUpHints(limitDistinctStrings(
                safe(vo.getFollowUpHints()),
                3,
                vo.getCollaborationSummary(),
                vo.getRecommendedOperatorAction(),
                vo.getProjectGovernanceLinkageSummary()));
        vo.setArchitectureFocusAreas(limitDistinctStrings(safe(vo.getArchitectureFocusAreas()), 3));
        vo.setDeliveryImplications(limitDistinctStrings(safe(vo.getDeliveryImplications()), 3));
        vo.setRisks(limitDistinctStrings(safe(vo.getRisks()), 3));
        vo.setOpenQuestions(limitDistinctStrings(safe(vo.getOpenQuestions()), 3));
        vo.setNextSteps(limitDistinctStrings(safe(vo.getNextSteps()), 3, vo.getRecommendedOperatorAction()));
        vo.setSolutionBrief(cleanText(vo.getSolutionBrief()));
        vo.setRoleInsights(normalizeRoleInsightMap(vo.getRoleInsights(),
                Map.of(
                        "productManagerView", "Product Manager：先锁首期可演示价值和范围边界。",
                        "architectView", "Architect：先确认系统边界、关键约束和实施顺序。")));
        return vo;
    }

    private List<ClarificationSuggestionVO> dedupeClarificationSuggestions(List<ClarificationSuggestionVO> suggestions) {
        Map<String, ClarificationSuggestionVO> deduped = new LinkedHashMap<>();
        for (ClarificationSuggestionVO suggestion : safe(suggestions)) {
            if (suggestion == null) {
                continue;
            }
            suggestion.setTitle(cleanText(suggestion.getTitle()));
            suggestion.setQuestion(cleanText(suggestion.getQuestion()));
            suggestion.setReason(cleanText(suggestion.getReason()));
            suggestion.setGovernanceReason(cleanText(suggestion.getGovernanceReason()));
            suggestion.setSuggestedOptions(cleanText(suggestion.getSuggestedOptions()));
            suggestion.setFollowUpModule(cleanText(suggestion.getFollowUpModule()));
            String key = normalizedTextKey(firstNonBlank(suggestion.getQuestion(), suggestion.getTitle()));
            if (!key.isBlank() && !deduped.containsKey(key)) {
                deduped.put(key, suggestion);
            }
        }
        return deduped.values().stream()
                .sorted((left, right) -> {
                    int blockerCompare = Integer.compare(
                            zeroIfNull(right.getBlockerFlag()),
                            zeroIfNull(left.getBlockerFlag()));
                    if (blockerCompare != 0) {
                        return blockerCompare;
                    }
                    int escalationCompare = Integer.compare(
                            zeroIfNull(right.getEscalationRecommended()),
                            zeroIfNull(left.getEscalationRecommended()));
                    if (escalationCompare != 0) {
                        return escalationCompare;
                    }
                    int severityCompare = Integer.compare(
                            severityPriority(left.getSeverity()),
                            severityPriority(right.getSeverity()));
                    if (severityCompare != 0) {
                        return severityCompare;
                    }
                    return firstNonBlank(left.getTitle(), left.getQuestion(), "")
                            .compareTo(firstNonBlank(right.getTitle(), right.getQuestion(), ""));
                })
                .limit(5)
                .toList();
    }

    private List<String> ensureRequirementRoleInsights(List<String> roleInsights) {
        List<String> cleaned = safe(roleInsights).stream()
                .map(this::cleanText)
                .filter(Objects::nonNull)
                .toList();
        List<String> result = new ArrayList<>();
        if (!cleaned.isEmpty()) {
            result.add(prefixIfMissing(cleaned.get(0), "Requirement Analyst"));
        }
        if (cleaned.size() > 1) {
            result.add(prefixIfMissing(cleaned.get(1), "Product Manager"));
        }
        if (result.isEmpty()) {
            result.add("Requirement Analyst：先补缺失输入，避免范围、验收和估算失真。");
            result.add("Product Manager：优先锁会改变 MVP、预算或里程碑承诺的问题。");
        } else if (result.size() == 1) {
            result.add("Product Manager：优先锁会改变 MVP、预算或里程碑承诺的问题。");
        }
        return result;
    }

    private Map<String, String> normalizeRoleInsightMap(
            Map<String, String> source,
            Map<String, String> defaults) {
        Map<String, String> result = new LinkedHashMap<>();
        Map<String, String> safeSource = source == null ? Map.of() : source;
        Map<String, String> fallbackMap = defaults == null ? Map.of() : defaults;
        for (Map.Entry<String, String> entry : fallbackMap.entrySet()) {
            String value = cleanText(firstNonBlank(safeSource.get(entry.getKey()), entry.getValue()));
            result.put(entry.getKey(), value);
        }
        return result;
    }

    private String buildRequirementCollaborationSummary(List<ClarificationSuggestionVO> suggestions) {
        long blockerCount = safe(suggestions).stream()
                .filter(item -> zeroIfNull(item.getBlockerFlag()) > 0)
                .count();
        long escalationCount = safe(suggestions).stream()
                .filter(item -> zeroIfNull(item.getEscalationRecommended()) > 0)
                .count();
        if (blockerCount > 0 || escalationCount > 0) {
            return "先识别阻塞澄清，再区分哪些问题留在澄清中心、哪些需要人工升级为正式确认。";
        }
        return "当前协作结果以补齐高影响输入为主，暂不需要扩大治理动作。";
    }

    private String buildRequirementOperatorAction(List<ClarificationSuggestionVO> suggestions) {
        long blockerCount = safe(suggestions).stream()
                .filter(item -> zeroIfNull(item.getBlockerFlag()) > 0)
                .count();
        long escalationCount = safe(suggestions).stream()
                .filter(item -> zeroIfNull(item.getEscalationRecommended()) > 0)
                .count();
        if (blockerCount > 0 && escalationCount > 0) {
            return "先应用阻塞澄清，再人工判断需要升级到决策中心的事项。";
        }
        if (blockerCount > 0) {
            return "先应用阻塞澄清，补齐目标、范围和约束后再继续下一轮确认。";
        }
        return "优先应用会影响 MVP、预算或里程碑承诺的事项，其余问题保留下一轮确认。";
    }

    private String buildDecisionBudgetOperatorAction(DecisionBudgetReviewVO vo) {
        if (zeroIfNull(vo.getBudgetConfirmationAdvised()) > 0) {
            return "先把推荐选项与预算影响写入当前决策，再人工判断是否补预算确认或审批。";
        }
        return "先更新当前决策记录并留档预算说明，暂不需要自动推进审批。";
    }

    private String buildProductArchitectureOperatorAction(ProductArchitectureBriefVO vo) {
        if (!safe(vo.getRisks()).isEmpty() || !safe(vo.getOpenQuestions()).isEmpty()) {
            return "先保存简报，再把风险最高或最不明确的 1-2 项送回澄清中心或决策中心。";
        }
        return "先保存简报作为项目记录，再把结论带入下一轮方案或设计评审。";
    }

    private List<String> limitDistinctStrings(List<String> source, int limit, String... excludes) {
        List<String> cleaned = excludeSimilarStrings(
                safe(source),
                excludes == null ? List.of() : Arrays.asList(excludes));
        Map<String, String> deduped = new LinkedHashMap<>();
        for (String value : cleaned) {
            String text = cleanText(value);
            if (text == null || text.isBlank()) {
                continue;
            }
            String key = normalizedTextKey(text);
            if (!key.isBlank() && !deduped.containsKey(key)) {
                deduped.put(key, text);
            }
        }
        return deduped.values().stream().limit(limit).toList();
    }

    private List<String> excludeSimilarStrings(List<String> source, List<String> excludes) {
        List<String> normalizedExcludes = safe(excludes).stream()
                .map(this::normalizedTextKey)
                .filter(value -> !value.isBlank())
                .toList();
        return safe(source).stream()
                .map(this::cleanText)
                .filter(Objects::nonNull)
                .filter(value -> {
                    String key = normalizedTextKey(value);
                    return normalizedExcludes.stream().noneMatch(exclude -> !exclude.isBlank() && exclude.equals(key));
                })
                .toList();
    }

    private String prefixIfMissing(String text, String roleLabel) {
        String cleaned = cleanText(text);
        if (cleaned == null || cleaned.isBlank()) {
            return roleLabel + "：-";
        }
        if (cleaned.contains("：") || cleaned.contains(":")) {
            return cleaned;
        }
        return roleLabel + "：" + cleaned;
    }

    private int severityPriority(String severity) {
        if ("blocker".equalsIgnoreCase(firstNonBlank(severity, ""))) {
            return 0;
        }
        if ("high".equalsIgnoreCase(firstNonBlank(severity, ""))) {
            return 1;
        }
        if ("medium".equalsIgnoreCase(firstNonBlank(severity, ""))) {
            return 2;
        }
        if ("low".equalsIgnoreCase(firstNonBlank(severity, ""))) {
            return 3;
        }
        return 9;
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("\\s+", " ").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String normalizedTextKey(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (char ch : value.toCharArray()) {
            if (Character.isLetterOrDigit(ch)) {
                builder.append(Character.toLowerCase(ch));
            }
        }
        return builder.toString();
    }

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer booleanToInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private Boolean toBoolean(Integer value) {
        if (value == null) {
            return null;
        }
        return value > 0;
    }

    private String latestStageCode(String projectId) {
        return projectStageService.lambdaQuery()
                .select(com.hiking.treasure.modules.phase1.entity.ProjectStage::getStageCode)
                .eq(com.hiking.treasure.modules.phase1.entity.ProjectStage::getProjectId, projectId)
                .orderByDesc(com.hiking.treasure.modules.phase1.entity.ProjectStage::getStageOrder)
                .last("LIMIT 1")
                .oneOpt()
                .map(com.hiking.treasure.modules.phase1.entity.ProjectStage::getStageCode)
                .orElse(null);
    }

    private String resolveCurrentStageCode(String projectId, String currentStageCode) {
        return isBlank(currentStageCode) ? latestStageCode(projectId) : currentStageCode;
    }

    private <T> List<T> safe(List<T> value) {
        return value == null ? List.of() : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String joinRemark(String... values) {
        return Arrays.stream(values == null ? new String[0] : values)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" | "));
    }

    private Map<String, Object> withGovernanceLinkage(
            Map<String, Object> original,
            String targetModule,
            String targetProjectId,
            String currentStageCode,
            Map<String, Object> extras) {
        Map<String, Object> linkage = new LinkedHashMap<>();
        if (original != null) {
            linkage.putAll(original);
        }
        linkage.put("targetModule", targetModule);
        linkage.put("targetProjectId", targetProjectId);
        linkage.put("currentStageCode", currentStageCode);
        if (extras != null) {
            linkage.putAll(extras);
        }
        return linkage;
    }
}
