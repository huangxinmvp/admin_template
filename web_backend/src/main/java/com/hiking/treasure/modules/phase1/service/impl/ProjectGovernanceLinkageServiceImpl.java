package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiking.treasure.modules.phase1.entity.AgentRole;
import com.hiking.treasure.modules.phase1.entity.AgentRoleStageParticipation;
import com.hiking.treasure.modules.phase1.entity.ApprovalRecord;
import com.hiking.treasure.modules.phase1.entity.BudgetPlan;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.entity.ProjectGovernanceState;
import com.hiking.treasure.modules.phase1.entity.ProjectStage;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplate;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplateStage;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentRoleStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentStageParticipationType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetHealthStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetPlanStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationSeverity;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionItemStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.GateStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectGovernanceStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectLifecycleStage;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectStageStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.RiskLevel;
import com.hiking.treasure.modules.phase1.mapper.AgentRoleMapper;
import com.hiking.treasure.modules.phase1.mapper.AgentRoleStageParticipationMapper;
import com.hiking.treasure.modules.phase1.mapper.ApprovalRecordMapper;
import com.hiking.treasure.modules.phase1.mapper.BudgetPlanMapper;
import com.hiking.treasure.modules.phase1.mapper.ClarificationItemMapper;
import com.hiking.treasure.modules.phase1.mapper.DecisionItemMapper;
import com.hiking.treasure.modules.phase1.mapper.ProjectGovernanceStateMapper;
import com.hiking.treasure.modules.phase1.mapper.ProjectMapper;
import com.hiking.treasure.modules.phase1.mapper.ProjectStageMapper;
import com.hiking.treasure.modules.phase1.mapper.RequirementIntakeMapper;
import com.hiking.treasure.modules.phase1.mapper.WorkflowTemplateMapper;
import com.hiking.treasure.modules.phase1.mapper.WorkflowTemplateStageMapper;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectGovernanceLinkageServiceImpl implements ProjectGovernanceLinkageService {

    private final ProjectMapper projectMapper;
    private final ProjectStageMapper projectStageMapper;
    private final RequirementIntakeMapper requirementIntakeMapper;
    private final ClarificationItemMapper clarificationItemMapper;
    private final DecisionItemMapper decisionItemMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final BudgetPlanMapper budgetPlanMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowTemplateStageMapper workflowTemplateStageMapper;
    private final AgentRoleMapper agentRoleMapper;
    private final AgentRoleStageParticipationMapper agentRoleStageParticipationMapper;
    private final ProjectGovernanceStateMapper projectGovernanceStateMapper;
    private final ProjectGovernanceStateService projectGovernanceStateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectGovernanceState recomputeProject(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            return null;
        }

        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            removeProjectState(projectId);
            return null;
        }

        List<ProjectStage> projectStages = listProjectStages(projectId);
        RequirementIntake requirementIntake = getRequirementIntake(projectId);
        List<ClarificationItem> clarificationItems = listClarificationItems(projectId);
        List<DecisionItem> decisionItems = listDecisionItems(projectId);
        List<ApprovalRecord> approvalRecords = listApprovalRecords(projectId);
        BudgetPlan latestBudgetPlan = getLatestBudgetPlan(projectId);
        WorkflowTemplate workflowTemplate = getWorkflowTemplate(project.getWorkflowTemplateId());
        List<WorkflowTemplateStage> workflowTemplateStages = listWorkflowTemplateStages(project.getWorkflowTemplateId());

        ResolvedStage currentStage = resolveCurrentStage(project, projectStages);
        DecisionSummary decisionSummary = buildDecisionSummary(decisionItems);
        ApprovalSummary approvalSummary = buildApprovalSummary(approvalRecords);
        RequirementSummary requirementSummary = buildRequirementSummary(requirementIntake, clarificationItems);
        BudgetSummary budgetSummary = buildBudgetSummary(latestBudgetPlan);
        WorkflowTemplateStage currentWorkflowTemplateStage = resolveWorkflowTemplateStage(
                currentStage.stageCode(),
                workflowTemplateStages
        );
        List<String> relevantStageCodes = resolveRelevantStageCodes(
                projectStages,
                workflowTemplateStages,
                currentStage.stageCode()
        );
        RoleCoverage roleCoverage = resolveRoleCoverage(relevantStageCodes, currentStage.stageCode());
        GateCoverage gateCoverage = buildGateCoverage(
                project,
                currentStage,
                workflowTemplate,
                currentWorkflowTemplateStage,
                decisionSummary,
                approvalSummary,
                budgetSummary
        );

        GovernanceResolution governanceResolution = resolveGovernanceState(
                project,
                currentStage,
                decisionSummary,
                approvalSummary,
                requirementSummary,
                budgetSummary,
                gateCoverage,
                roleCoverage
        );

        ProjectGovernanceState state = getExistingState(projectId);
        if (state == null) {
            state = new ProjectGovernanceState();
            state.setProjectId(projectId);
        }
        state.setCurrentStageCode(currentStage.stageCode());
        state.setCurrentStageName(currentStage.stageName());
        state.setCurrentStageStatus(currentStage.stageStatus());
        state.setCurrentGateStatus(currentStage.gateStatus());
        state.setGovernanceStatus(governanceResolution.governanceStatus());
        state.setBlockedFlag(governanceResolution.blocked() ? 1 : 0);
        state.setAtRiskFlag(governanceResolution.atRisk() ? 1 : 0);
        state.setPendingDecisionCount(decisionSummary.pendingCount());
        state.setBlockerDecisionCount(decisionSummary.blockerCount());
        state.setPendingApprovalCount(approvalSummary.pendingCount());
        state.setBlockerApprovalCount(approvalSummary.blockerCount());
        state.setClarificationCount(requirementSummary.clarificationCount());
        state.setBlockerClarificationCount(requirementSummary.blockerClarificationCount());
        state.setFailedGateConditionCount(gateCoverage.failedGateConditionCount());
        state.setBlockingGateConditionCount(gateCoverage.blockingGateConditionCount());
        state.setMissingCriticalRoleCount(roleCoverage.missingCriticalRoleCount());
        state.setBudgetStatus(budgetSummary.status());
        state.setBlockerReasonSummary(governanceResolution.reasonSummary());
        state.setLastRecomputedAt(LocalDateTime.now());

        if (state.getId() == null || state.getId().isBlank()) {
            projectGovernanceStateService.save(state);
        } else {
            projectGovernanceStateService.updateById(state);
        }
        return getExistingState(projectId);
    }

    @Override
    public ProjectGovernanceState getOrRecomputeProjectState(String projectId) {
        ProjectGovernanceState existing = getExistingState(projectId);
        return existing != null ? existing : recomputeProject(projectId);
    }

    @Override
    public Map<String, ProjectGovernanceState> loadOrRecomputeProjectStates(Collection<String> projectIds) {
        List<String> resolvedProjectIds = normalizeIds(projectIds);
        if (resolvedProjectIds.isEmpty()) {
            return Map.of();
        }

        Map<String, ProjectGovernanceState> stateMap = listStates(resolvedProjectIds).stream()
                .collect(LinkedHashMap::new, (map, state) -> map.put(state.getProjectId(), state), Map::putAll);

        List<String> missingProjectIds = resolvedProjectIds.stream()
                .filter(projectId -> !stateMap.containsKey(projectId))
                .toList();
        for (String missingProjectId : missingProjectIds) {
            ProjectGovernanceState state = recomputeProject(missingProjectId);
            if (state != null) {
                stateMap.put(missingProjectId, state);
            }
        }
        return stateMap;
    }

    @Override
    public void recomputeProjects(Collection<String> projectIds) {
        for (String projectId : normalizeIds(projectIds)) {
            recomputeProject(projectId);
        }
    }

    @Override
    public void recomputeProjectsByWorkflowTemplate(String workflowTemplateId) {
        if (workflowTemplateId == null || workflowTemplateId.isBlank()) {
            return;
        }
        List<String> projectIds = projectMapper.selectList(new LambdaQueryWrapper<Project>()
                        .eq(Project::getWorkflowTemplateId, workflowTemplateId))
                .stream()
                .map(Project::getId)
                .filter(Objects::nonNull)
                .toList();
        recomputeProjects(projectIds);
    }

    @Override
    public void recomputeAllProjects() {
        recomputeProjects(projectMapper.selectList(new LambdaQueryWrapper<Project>()
                        .select(Project::getId))
                .stream()
                .map(Project::getId)
                .filter(Objects::nonNull)
                .toList());
    }

    @Override
    public void removeProjectState(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            return;
        }
        projectGovernanceStateService.remove(new LambdaQueryWrapper<ProjectGovernanceState>()
                .eq(ProjectGovernanceState::getProjectId, projectId));
    }

    private List<ProjectStage> listProjectStages(String projectId) {
        return projectStageMapper.selectList(new LambdaQueryWrapper<ProjectStage>()
                .eq(ProjectStage::getProjectId, projectId)
                .orderByAsc(ProjectStage::getStageOrder)
                .orderByAsc(ProjectStage::getCreateTime));
    }

    private RequirementIntake getRequirementIntake(String projectId) {
        return requirementIntakeMapper.selectOne(new LambdaQueryWrapper<RequirementIntake>()
                .eq(RequirementIntake::getProjectId, projectId)
                .last("LIMIT 1"));
    }

    private List<ClarificationItem> listClarificationItems(String projectId) {
        return clarificationItemMapper.selectList(new LambdaQueryWrapper<ClarificationItem>()
                .eq(ClarificationItem::getProjectId, projectId)
                .orderByDesc(ClarificationItem::getUpdateTime)
                .orderByDesc(ClarificationItem::getCreateTime));
    }

    private List<DecisionItem> listDecisionItems(String projectId) {
        return decisionItemMapper.selectList(new LambdaQueryWrapper<DecisionItem>()
                .eq(DecisionItem::getProjectId, projectId)
                .orderByDesc(DecisionItem::getUpdateTime)
                .orderByDesc(DecisionItem::getCreateTime));
    }

    private List<ApprovalRecord> listApprovalRecords(String projectId) {
        return approvalRecordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getProjectId, projectId)
                .orderByDesc(ApprovalRecord::getDecidedAt)
                .orderByDesc(ApprovalRecord::getSubmittedAt)
                .orderByDesc(ApprovalRecord::getCreateTime));
    }

    private BudgetPlan getLatestBudgetPlan(String projectId) {
        return budgetPlanMapper.selectOne(new LambdaQueryWrapper<BudgetPlan>()
                .eq(BudgetPlan::getProjectId, projectId)
                .orderByDesc(BudgetPlan::getEffectiveAt)
                .orderByDesc(BudgetPlan::getUpdateTime)
                .orderByDesc(BudgetPlan::getCreateTime)
                .last("LIMIT 1"));
    }

    private WorkflowTemplate getWorkflowTemplate(String workflowTemplateId) {
        if (workflowTemplateId == null || workflowTemplateId.isBlank()) {
            return null;
        }
        return workflowTemplateMapper.selectById(workflowTemplateId);
    }

    private List<WorkflowTemplateStage> listWorkflowTemplateStages(String workflowTemplateId) {
        if (workflowTemplateId == null || workflowTemplateId.isBlank()) {
            return List.of();
        }
        return workflowTemplateStageMapper.selectList(new LambdaQueryWrapper<WorkflowTemplateStage>()
                .eq(WorkflowTemplateStage::getTemplateId, workflowTemplateId)
                .orderByAsc(WorkflowTemplateStage::getStageOrder)
                .orderByAsc(WorkflowTemplateStage::getCreateTime));
    }

    private ResolvedStage resolveCurrentStage(Project project, List<ProjectStage> stages) {
        List<ProjectStage> safeStages = safe(stages);
        ProjectStage activeStage = safeStages.stream()
                .filter(stage -> Objects.equals(stage.getStageStatus(), ProjectStageStatus.ACTIVE.getCode()))
                .findFirst()
                .orElse(null);
        ProjectStage matchedStage = safeStages.stream()
                .filter(stage -> Objects.equals(stage.getStageCode(), project.getCurrentStageCode()))
                .findFirst()
                .orElse(null);
        ProjectStage fallbackStage = safeStages.stream()
                .max(Comparator
                        .comparing((ProjectStage stage) -> firstNonNull(stage.getStageOrder(), 0))
                        .thenComparing(stage -> firstNonNull(stage.getUpdateTime(), stage.getCreateTime()),
                                Comparator.nullsLast(LocalDateTime::compareTo)))
                .orElse(null);

        ProjectStage resolved = firstNonNull(activeStage, matchedStage, fallbackStage);
        String stageCode = resolved != null ? resolved.getStageCode() : project.getCurrentStageCode();
        String stageName = resolved != null
                ? firstNonBlank(resolved.getStageName(), lifecycleStageLabel(stageCode), stageCode)
                : firstNonBlank(lifecycleStageLabel(stageCode), stageCode, "-");
        return new ResolvedStage(
                stageCode,
                stageName,
                resolved == null ? null : resolved.getStageStatus(),
                resolved == null ? null : resolved.getGateStatus()
        );
    }

    private DecisionSummary buildDecisionSummary(List<DecisionItem> decisionItems) {
        int pendingCount = (int) safe(decisionItems).stream()
                .filter(item -> Objects.equals(item.getStatus(), DecisionItemStatus.OPEN.getCode())
                        || Objects.equals(item.getStatus(), DecisionItemStatus.PENDING_APPROVAL.getCode()))
                .count();
        int blockerCount = (int) safe(decisionItems).stream()
                .filter(item -> Objects.equals(item.getBlockerFlag(), 1))
                .filter(item -> Objects.equals(item.getStatus(), DecisionItemStatus.OPEN.getCode())
                        || Objects.equals(item.getStatus(), DecisionItemStatus.PENDING_APPROVAL.getCode()))
                .count();
        return new DecisionSummary(pendingCount, blockerCount);
    }

    private ApprovalSummary buildApprovalSummary(List<ApprovalRecord> approvalRecords) {
        int pendingCount = (int) safe(approvalRecords).stream()
                .filter(this::isPendingApproval)
                .count();
        int blockerCount = (int) safe(approvalRecords).stream()
                .filter(this::isPendingApproval)
                .filter(record -> Objects.equals(record.getBlockerFlag(), 1))
                .count();
        return new ApprovalSummary(pendingCount, blockerCount);
    }

    private RequirementSummary buildRequirementSummary(
            RequirementIntake requirementIntake,
            List<ClarificationItem> clarificationItems) {
        int clarificationCount = safe(clarificationItems).size();
        int blockerClarificationCount = (int) safe(clarificationItems).stream()
                .filter(item -> !Objects.equals(item.getStatus(), ClarificationStatus.RESOLVED.getCode()))
                .filter(item -> Objects.equals(item.getSeverity(), ClarificationSeverity.BLOCKER.getCode()))
                .count();
        return new RequirementSummary(
                clarificationCount,
                blockerClarificationCount,
                calculateCompletenessScore(requirementIntake)
        );
    }

    private int calculateCompletenessScore(RequirementIntake requirementIntake) {
        int completedFieldCount = 0;
        completedFieldCount += isPresent(requirementIntake == null ? null : requirementIntake.getBusinessGoal()) ? 1 : 0;
        completedFieldCount += isPresent(requirementIntake == null ? null : requirementIntake.getFeatureSummary()) ? 1 : 0;
        completedFieldCount += isPresent(requirementIntake == null ? null : requirementIntake.getReferenceProducts()) ? 1 : 0;
        completedFieldCount += isPresent(requirementIntake == null ? null : requirementIntake.getTimelineExpectation()) ? 1 : 0;
        completedFieldCount += isPresent(requirementIntake == null ? null : requirementIntake.getBudgetRange()) ? 1 : 0;
        completedFieldCount += isPresent(requirementIntake == null ? null : requirementIntake.getTechnicalConstraints()) ? 1 : 0;
        return (completedFieldCount * 100) / 6;
    }

    private BudgetSummary buildBudgetSummary(BudgetPlan budgetPlan) {
        if (budgetPlan == null) {
            return new BudgetSummary(BudgetHealthStatus.UNPLANNED.getCode());
        }

        long proposedAmount = zeroIfNull(budgetPlan.getProposedAmount());
        long approvedAmount = zeroIfNull(budgetPlan.getApprovedAmount());
        long consumedAmount = zeroIfNull(budgetPlan.getConsumedAmount());

        if (Objects.equals(budgetPlan.getStatus(), BudgetPlanStatus.DRAFT.getCode())
                || Objects.equals(budgetPlan.getStatus(), BudgetPlanStatus.PENDING_APPROVAL.getCode())) {
            return new BudgetSummary(BudgetHealthStatus.PENDING.getCode());
        }
        if (approvedAmount <= 0L) {
            return new BudgetSummary(BudgetHealthStatus.PENDING.getCode());
        }
        if (consumedAmount >= approvedAmount) {
            return new BudgetSummary(BudgetHealthStatus.OVERRUN.getCode());
        }
        if (consumedAmount * 100 >= approvedAmount * 80) {
            return new BudgetSummary(BudgetHealthStatus.WARNING.getCode());
        }
        if (proposedAmount > approvedAmount) {
            return new BudgetSummary(BudgetHealthStatus.WARNING.getCode());
        }
        return new BudgetSummary(BudgetHealthStatus.HEALTHY.getCode());
    }

    private WorkflowTemplateStage resolveWorkflowTemplateStage(
            String currentStageCode,
            List<WorkflowTemplateStage> workflowTemplateStages) {
        List<WorkflowTemplateStage> safeStages = safe(workflowTemplateStages);
        return safeStages.stream()
                .filter(stage -> Objects.equals(stage.getStageCode(), currentStageCode))
                .findFirst()
                .orElseGet(() -> safeStages.stream()
                        .filter(stage -> Objects.equals(stage.getEnabledFlag(), 1))
                        .findFirst()
                        .orElse(safeStages.stream().findFirst().orElse(null)));
    }

    private List<String> resolveRelevantStageCodes(
            List<ProjectStage> projectStages,
            List<WorkflowTemplateStage> workflowTemplateStages,
            String currentStageCode) {
        List<String> templateStageCodes = safe(workflowTemplateStages).stream()
                .filter(stage -> Objects.equals(stage.getEnabledFlag(), 1))
                .map(WorkflowTemplateStage::getStageCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!templateStageCodes.isEmpty()) {
            return templateStageCodes;
        }

        List<String> projectStageCodes = safe(projectStages).stream()
                .map(ProjectStage::getStageCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!projectStageCodes.isEmpty()) {
            return projectStageCodes;
        }

        return isPresent(currentStageCode) ? List.of(currentStageCode) : List.of();
    }

    private RoleCoverage resolveRoleCoverage(List<String> relevantStageCodes, String currentStageCode) {
        List<AgentRole> enabledRoles = agentRoleMapper.selectList(new LambdaQueryWrapper<AgentRole>()
                .eq(AgentRole::getStatus, AgentRoleStatus.ACTIVE.getCode())
                .orderByDesc(AgentRole::getDefaultFlag)
                .orderByAsc(AgentRole::getRoleCategory)
                .orderByAsc(AgentRole::getRoleName));
        if (enabledRoles.isEmpty()) {
            return new RoleCoverage(0, List.of("当前项目没有可用的启用 Agent 角色"));
        }

        List<String> roleIds = enabledRoles.stream()
                .map(AgentRole::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<String, List<AgentRoleStageParticipation>> participationsByRole = loadStageParticipations(roleIds);

        boolean hasLinkedRole = enabledRoles.stream()
                .anyMatch(role -> participatesInRelevantStages(participationsByRole.get(role.getId()), relevantStageCodes, currentStageCode));
        boolean hasRequiredRole = enabledRoles.stream()
                .anyMatch(role -> Objects.equals(
                        resolveCurrentStageParticipationType(participationsByRole.get(role.getId()), currentStageCode),
                        AgentStageParticipationType.REQUIRED.getCode()));

        List<String> missingCriticalRoles = new ArrayList<>();
        if (!hasRequiredRole) {
            missingCriticalRoles.add("当前阶段未配置必需的 Agent 角色");
        }
        if (!hasLinkedRole) {
            missingCriticalRoles.add("当前项目没有与流程阶段匹配的 Agent 角色");
        }
        return new RoleCoverage(missingCriticalRoles.size(), missingCriticalRoles);
    }

    private Map<String, List<AgentRoleStageParticipation>> loadStageParticipations(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        Map<String, List<AgentRoleStageParticipation>> grouped = new LinkedHashMap<>();
        agentRoleStageParticipationMapper.selectList(new LambdaQueryWrapper<AgentRoleStageParticipation>()
                        .in(AgentRoleStageParticipation::getAgentRoleId, roleIds)
                        .orderByAsc(AgentRoleStageParticipation::getStageCode))
                .forEach(item -> grouped.computeIfAbsent(item.getAgentRoleId(), key -> new ArrayList<>()).add(item));
        return grouped;
    }

    private boolean participatesInRelevantStages(
            List<AgentRoleStageParticipation> participations,
            List<String> relevantStageCodes,
            String currentStageCode) {
        if (participations == null || participations.isEmpty()) {
            return false;
        }
        return participations.stream()
                .filter(item -> !Objects.equals(item.getParticipationType(), AgentStageParticipationType.NOT_INVOLVED.getCode()))
                .anyMatch(item -> relevantStageCodes.contains(item.getStageCode())
                        || Objects.equals(item.getStageCode(), currentStageCode));
    }

    private String resolveCurrentStageParticipationType(
            List<AgentRoleStageParticipation> participations,
            String currentStageCode) {
        if (!isPresent(currentStageCode) || participations == null || participations.isEmpty()) {
            return AgentStageParticipationType.NOT_INVOLVED.getCode();
        }
        return participations.stream()
                .filter(item -> Objects.equals(item.getStageCode(), currentStageCode))
                .map(AgentRoleStageParticipation::getParticipationType)
                .findFirst()
                .orElse(AgentStageParticipationType.NOT_INVOLVED.getCode());
    }

    private GateCoverage buildGateCoverage(
            Project project,
            ResolvedStage currentStage,
            WorkflowTemplate workflowTemplate,
            WorkflowTemplateStage currentWorkflowTemplateStage,
            DecisionSummary decisionSummary,
            ApprovalSummary approvalSummary,
            BudgetSummary budgetSummary) {
        if (workflowTemplate == null) {
            return new GateCoverage(0, 0);
        }

        int blockedCount = 0;
        int failedCount = 0;

        blockedCount += countGateCondition(
                workflowTemplate.getGateChecksConfig(),
                resolveGateCheckStatus(currentStage)
        );
        blockedCount += countGateCondition(
                workflowTemplate.getBlockingDecisionConfig(),
                decisionSummary.blockerCount() > 0
                        ? "blocked"
                        : decisionSummary.pendingCount() > 0 ? "warning" : "pass"
        );
        blockedCount += countGateCondition(
                workflowTemplate.getBlockingApprovalConfig(),
                approvalSummary.blockerCount() > 0
                        ? "blocked"
                        : approvalSummary.pendingCount() > 0 ? "warning" : "pass"
        );
        blockedCount += countGateCondition(
                workflowTemplate.getBudgetThresholdConfig(),
                resolveBudgetGateStatus(budgetSummary.status())
        );
        blockedCount += countGateCondition(
                workflowTemplate.getHighRiskApprovalConfig(),
                resolveHighRiskGateStatus(project, approvalSummary)
        );

        failedCount += countGateCondition(
                workflowTemplate.getGateChecksConfig(),
                resolveGateCheckStatus(currentStage)
        );
        failedCount += countGateCondition(
                workflowTemplate.getBlockingDecisionConfig(),
                decisionSummary.blockerCount() > 0 ? "blocked" : "pass"
        );
        failedCount += countGateCondition(
                workflowTemplate.getBlockingApprovalConfig(),
                approvalSummary.blockerCount() > 0 ? "blocked" : "pass"
        );
        failedCount += countGateCondition(
                workflowTemplate.getBudgetThresholdConfig(),
                resolveBudgetGateStatus(budgetSummary.status())
        );
        failedCount += countGateCondition(
                workflowTemplate.getHighRiskApprovalConfig(),
                resolveHighRiskGateStatus(project, approvalSummary)
        );

        if (currentWorkflowTemplateStage == null && hasAnyGateConfig(workflowTemplate)) {
            blockedCount += 1;
            failedCount += 1;
        }

        return new GateCoverage(blockedCount, failedCount);
    }

    private boolean hasAnyGateConfig(WorkflowTemplate workflowTemplate) {
        return workflowTemplate != null
                && (isPresent(workflowTemplate.getGateChecksConfig())
                || isPresent(workflowTemplate.getBlockingDecisionConfig())
                || isPresent(workflowTemplate.getBlockingApprovalConfig())
                || isPresent(workflowTemplate.getBudgetThresholdConfig())
                || isPresent(workflowTemplate.getHighRiskApprovalConfig()));
    }

    private int countGateCondition(String configValue, String status) {
        if (!isPresent(configValue)) {
            return 0;
        }
        return Objects.equals(status, "blocked") ? 1 : 0;
    }

    private String resolveGateCheckStatus(ResolvedStage currentStage) {
        if (currentStage == null) {
            return "configured";
        }
        if (Objects.equals(currentStage.gateStatus(), GateStatus.REJECTED.getCode())
                || Objects.equals(currentStage.stageStatus(), ProjectStageStatus.BLOCKED.getCode())) {
            return "blocked";
        }
        if (Objects.equals(currentStage.gateStatus(), GateStatus.PENDING.getCode())) {
            return "warning";
        }
        if (Objects.equals(currentStage.gateStatus(), GateStatus.APPROVED.getCode())
                || Objects.equals(currentStage.gateStatus(), GateStatus.NOT_REQUIRED.getCode())) {
            return "pass";
        }
        return "configured";
    }

    private String resolveBudgetGateStatus(String budgetStatus) {
        if (Objects.equals(budgetStatus, BudgetHealthStatus.OVERRUN.getCode())) {
            return "blocked";
        }
        if (Objects.equals(budgetStatus, BudgetHealthStatus.WARNING.getCode())
                || Objects.equals(budgetStatus, BudgetHealthStatus.PENDING.getCode())
                || Objects.equals(budgetStatus, BudgetHealthStatus.UNPLANNED.getCode())) {
            return "warning";
        }
        return "pass";
    }

    private String resolveHighRiskGateStatus(Project project, ApprovalSummary approvalSummary) {
        boolean highRisk = Objects.equals(project.getRiskLevel(), RiskLevel.HIGH.getCode())
                || Objects.equals(project.getRiskLevel(), RiskLevel.CRITICAL.getCode());
        if (!highRisk) {
            return "pass";
        }
        if (approvalSummary.blockerCount() > 0) {
            return "blocked";
        }
        if (approvalSummary.pendingCount() > 0) {
            return "warning";
        }
        return "warning";
    }

    private GovernanceResolution resolveGovernanceState(
            Project project,
            ResolvedStage currentStage,
            DecisionSummary decisionSummary,
            ApprovalSummary approvalSummary,
            RequirementSummary requirementSummary,
            BudgetSummary budgetSummary,
            GateCoverage gateCoverage,
            RoleCoverage roleCoverage) {
        List<String> blockedReasons = new ArrayList<>();
        if (Objects.equals(currentStage.stageStatus(), ProjectStageStatus.BLOCKED.getCode())) {
            blockedReasons.add("当前阶段阻塞");
        }
        if (requirementSummary.blockerClarificationCount() > 0) {
            blockedReasons.add("阻塞澄清项 " + requirementSummary.blockerClarificationCount());
        }
        if (decisionSummary.blockerCount() > 0) {
            blockedReasons.add("阻塞决策 " + decisionSummary.blockerCount());
        }
        if (approvalSummary.blockerCount() > 0) {
            blockedReasons.add("阻塞审批 " + approvalSummary.blockerCount());
        }
        if (gateCoverage.failedGateConditionCount() > 0) {
            blockedReasons.add("失败门禁条件 " + gateCoverage.failedGateConditionCount());
        }
        if (roleCoverage.missingCriticalRoleCount() > 0) {
            blockedReasons.add("缺失关键角色 " + roleCoverage.missingCriticalRoleCount());
        }
        if (Objects.equals(budgetSummary.status(), BudgetHealthStatus.OVERRUN.getCode())) {
            blockedReasons.add("预算超支");
        }

        boolean blocked = !blockedReasons.isEmpty();

        List<String> riskReasons = new ArrayList<>();
        if (Objects.equals(project.getRiskLevel(), RiskLevel.HIGH.getCode())
                || Objects.equals(project.getRiskLevel(), RiskLevel.CRITICAL.getCode())) {
            riskReasons.add("项目风险等级高");
        }
        if (decisionSummary.pendingCount() > 0) {
            riskReasons.add("待处理决策 " + decisionSummary.pendingCount());
        }
        if (approvalSummary.pendingCount() > 0) {
            riskReasons.add("待处理审批 " + approvalSummary.pendingCount());
        }
        if (Objects.equals(currentStage.gateStatus(), GateStatus.PENDING.getCode())) {
            riskReasons.add("当前门禁待确认");
        }
        if (Objects.equals(budgetSummary.status(), BudgetHealthStatus.WARNING.getCode())) {
            riskReasons.add("预算预警");
        }
        if (Objects.equals(budgetSummary.status(), BudgetHealthStatus.PENDING.getCode())) {
            riskReasons.add("预算待确认");
        }
        if (Objects.equals(budgetSummary.status(), BudgetHealthStatus.UNPLANNED.getCode())) {
            riskReasons.add("预算未规划");
        }

        boolean atRisk = !blocked && !riskReasons.isEmpty();
        String governanceStatus = blocked
                ? ProjectGovernanceStatus.BLOCKED.getCode()
                : atRisk ? ProjectGovernanceStatus.AT_RISK.getCode() : ProjectGovernanceStatus.HEALTHY.getCode();

        List<String> reasonSource = blocked ? blockedReasons : riskReasons;
        String reasonSummary = reasonSource.isEmpty()
                ? "治理状态正常"
                : reasonSource.stream().limit(3).collect(java.util.stream.Collectors.joining(" / "));

        return new GovernanceResolution(governanceStatus, blocked, atRisk, reasonSummary);
    }

    private boolean isPendingApproval(ApprovalRecord approvalRecord) {
        return approvalRecord != null
                && (Objects.equals(approvalRecord.getApprovalStatus(), ApprovalStatus.DRAFT.getCode())
                || Objects.equals(approvalRecord.getApprovalStatus(), ApprovalStatus.SUBMITTED.getCode())
                || Objects.equals(approvalRecord.getApprovalStatus(), ApprovalStatus.REQUEST_CHANGES.getCode())
                || Objects.equals(approvalRecord.getApprovalStatus(), ApprovalStatus.DEFERRED.getCode()));
    }

    private String lifecycleStageLabel(String code) {
        if (!isPresent(code)) {
            return null;
        }
        for (ProjectLifecycleStage stage : ProjectLifecycleStage.values()) {
            if (Objects.equals(stage.getCode(), code)) {
                return stage.getLabel();
            }
        }
        return code;
    }

    private ProjectGovernanceState getExistingState(String projectId) {
        return projectGovernanceStateMapper.selectOne(new LambdaQueryWrapper<ProjectGovernanceState>()
                .eq(ProjectGovernanceState::getProjectId, projectId)
                .last("LIMIT 1"));
    }

    private List<ProjectGovernanceState> listStates(List<String> projectIds) {
        return projectGovernanceStateMapper.selectList(new LambdaQueryWrapper<ProjectGovernanceState>()
                .in(ProjectGovernanceState::getProjectId, projectIds));
    }

    private List<String> normalizeIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = ids.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        return new ArrayList<>(normalized);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }

    private <T> List<T> safe(List<T> items) {
        return items == null ? List.of() : items;
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record ResolvedStage(String stageCode, String stageName, String stageStatus, String gateStatus) {
    }

    private record DecisionSummary(int pendingCount, int blockerCount) {
    }

    private record ApprovalSummary(int pendingCount, int blockerCount) {
    }

    private record RequirementSummary(int clarificationCount, int blockerClarificationCount, int completenessScore) {
    }

    private record BudgetSummary(String status) {
    }

    private record RoleCoverage(int missingCriticalRoleCount, List<String> missingCriticalRoles) {
    }

    private record GateCoverage(int blockingGateConditionCount, int failedGateConditionCount) {
    }

    private record GovernanceResolution(
            String governanceStatus,
            boolean blocked,
            boolean atRisk,
            String reasonSummary) {
    }
}
