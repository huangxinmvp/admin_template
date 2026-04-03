package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
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
import com.hiking.treasure.modules.phase1.domain.vo.ProjectActivityVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectApprovalSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetLedgerVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectBudgetSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterListVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectDecisionSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectDecisionBudgetCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectAgentRoleSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectGateConditionVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectGovernanceSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectNextStepGuidanceVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectProductArchitectureCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectRequirementCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectRequirementSummaryVO;
import com.hiking.treasure.modules.phase1.entity.AgentRole;
import com.hiking.treasure.modules.phase1.entity.AgentRoleAllowedAction;
import com.hiking.treasure.modules.phase1.entity.AgentRoleStageParticipation;
import com.hiking.treasure.modules.phase1.entity.ApprovalRecord;
import com.hiking.treasure.modules.phase1.entity.BudgetLedger;
import com.hiking.treasure.modules.phase1.entity.BudgetPlan;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.entity.MeetingRecord;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.entity.ProjectGovernanceState;
import com.hiking.treasure.modules.phase1.entity.ProjectStage;
import com.hiking.treasure.modules.phase1.entity.ProjectToolBinding;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import com.hiking.treasure.modules.phase1.entity.ToolIntegrationAudit;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplate;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplateStage;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationSeverity;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetHealthStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetPlanStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionItemStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentRoleStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentStageParticipationType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectLifecycleStage;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectStageStatus;
import com.hiking.treasure.modules.phase1.mapper.ProjectMapper;
import com.hiking.treasure.modules.phase1.service.AgentRoleAllowedActionService;
import com.hiking.treasure.modules.phase1.service.AgentRoleService;
import com.hiking.treasure.modules.phase1.service.AgentRoleStageParticipationService;
import com.hiking.treasure.modules.phase1.service.ApprovalRecordService;
import com.hiking.treasure.modules.phase1.service.BudgetLedgerService;
import com.hiking.treasure.modules.phase1.service.BudgetPlanService;
import com.hiking.treasure.modules.phase1.service.ClarificationItemService;
import com.hiking.treasure.modules.phase1.service.DecisionItemService;
import com.hiking.treasure.modules.phase1.service.MeetingRecordService;
import com.hiking.treasure.modules.phase1.service.ProjectService;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.ProjectStageService;
import com.hiking.treasure.modules.phase1.service.ProjectToolBindingService;
import com.hiking.treasure.modules.phase1.service.RequirementIntakeService;
import com.hiking.treasure.modules.phase1.service.ToolIntegrationAuditService;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateService;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateStageService;
import com.hiking.treasure.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private static final int DETAIL_ITEM_LIMIT = 6;
    private static final int ACTIVITY_LIMIT = 10;

    private final ProjectConvert projectConvert;
    private final ProjectStageConvert projectStageConvert;
    private final RequirementIntakeConvert requirementIntakeConvert;
    private final ClarificationItemConvert clarificationItemConvert;
    private final DecisionItemConvert decisionItemConvert;
    private final MeetingRecordConvert meetingRecordConvert;
    private final ApprovalRecordConvert approvalRecordConvert;
    private final BudgetLedgerConvert budgetLedgerConvert;
    private final ProjectToolBindingConvert projectToolBindingConvert;
    private final ToolIntegrationAuditConvert toolIntegrationAuditConvert;
    private final ProjectStageService projectStageService;
    private final RequirementIntakeService requirementIntakeService;
    private final ClarificationItemService clarificationItemService;
    private final DecisionItemService decisionItemService;
    private final MeetingRecordService meetingRecordService;
    private final ApprovalRecordService approvalRecordService;
    private final ProjectToolBindingService projectToolBindingService;
    private final ToolIntegrationAuditService toolIntegrationAuditService;
    private final AgentRoleService agentRoleService;
    private final AgentRoleStageParticipationService agentRoleStageParticipationService;
    private final AgentRoleAllowedActionService agentRoleAllowedActionService;
    private final BudgetPlanService budgetPlanService;
    private final BudgetLedgerService budgetLedgerService;
    private final WorkflowTemplateService workflowTemplateService;
    private final WorkflowTemplateStageService workflowTemplateStageService;
    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;
    private final UserService userService;
    private final ProjectNextStepGuidanceResolver nextStepGuidanceResolver = new ProjectNextStepGuidanceResolver();

    @Override
    public boolean save(Project entity) {
        boolean saved = super.save(entity);
        if (saved) {
            projectGovernanceLinkageService.recomputeProject(entity == null ? null : entity.getId());
        }
        return saved;
    }

    @Override
    public boolean updateById(Project entity) {
        String projectId = entity == null ? null : entity.getId();
        boolean updated = super.updateById(entity);
        if (updated) {
            projectGovernanceLinkageService.recomputeProject(projectId);
        }
        return updated;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        String projectId = Objects.toString(id, null);
        boolean removed = super.removeById(id);
        if (removed) {
            projectGovernanceLinkageService.removeProjectState(projectId);
        }
        return removed;
    }

    @Override
    public boolean removeByIds(Collection<?> list) {
        List<java.io.Serializable> ids = normalizeIds(list);
        List<String> projectIds = listByIds(ids).stream()
                .map(Project::getId)
                .filter(Objects::nonNull)
                .toList();
        boolean removed = super.removeByIds(ids);
        if (removed) {
            projectIds.forEach(projectGovernanceLinkageService::removeProjectState);
        }
        return removed;
    }

    @Override
    public Page<ProjectCenterListVO> pageProjectCenter(ProjectCenterQueryDTO dto, long pageNo, long pageSize) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
                wrapper.and(q -> q.like(Project::getProjectName, dto.getKeyword())
                        .or()
                        .like(Project::getProjectCode, dto.getKeyword()));
            }
            if (dto.getProjectType() != null && !dto.getProjectType().isBlank()) {
                wrapper.eq(Project::getProjectType, dto.getProjectType());
            }
            if (dto.getCurrentStageCode() != null && !dto.getCurrentStageCode().isBlank()) {
                wrapper.eq(Project::getCurrentStageCode, dto.getCurrentStageCode());
            }
            if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
                wrapper.eq(Project::getStatus, dto.getStatus());
            }
            if (dto.getRiskLevel() != null && !dto.getRiskLevel().isBlank()) {
                wrapper.eq(Project::getRiskLevel, dto.getRiskLevel());
            }
        }
        wrapper.orderByDesc(Project::getUpdateTime).orderByDesc(Project::getCreateTime);

        Page<Project> page = page(new Page<>(pageNo, pageSize), wrapper);
        Page<ProjectCenterListVO> result = new Page<>(pageNo, pageSize, page.getTotal());
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            result.setRecords(List.of());
            return result;
        }

        List<Project> projects = page.getRecords();
        List<String> projectIds = projects.stream().map(Project::getId).filter(Objects::nonNull).toList();
        Map<String, ProjectGovernanceState> governanceStateByProject =
                projectGovernanceLinkageService.loadOrRecomputeProjectStates(projectIds);
        Map<String, List<ProjectStage>> stagesByProject = loadStagesByProject(projectIds);
        Map<String, List<DecisionItem>> decisionsByProject = loadDecisionsByProject(projectIds);
        Map<String, BudgetPlan> latestBudgetPlanByProject = loadLatestBudgetPlans(projectIds);
        Map<String, String> ownerDisplayMap = loadUserDisplayMap(
                projects.stream().map(Project::getOwnerUserId).filter(Objects::nonNull).toList());

        List<ProjectCenterListVO> records = projects.stream().map(project -> {
            ProjectGovernanceState governanceState = governanceStateByProject.get(project.getId());
            ResolvedStage currentStage = resolveCurrentStage(project, stagesByProject.get(project.getId()));
            ProjectBudgetSummaryVO budgetSummary = buildBudgetSummary(latestBudgetPlanByProject.get(project.getId()));
            ProjectGovernanceSummaryVO guidanceGovernanceSummary = buildListGovernanceSummary(
                    governanceState,
                    currentStage,
                    budgetSummary,
                    countPendingDecisions(decisionsByProject.get(project.getId())),
                    countBlockerPendingDecisions(decisionsByProject.get(project.getId()))
            );
            ProjectNextStepGuidanceVO nextStepGuidance = nextStepGuidanceResolver.resolve(
                    new ProjectNextStepGuidanceResolver.ProjectNextStepContext(
                            currentStage.stageCode(),
                            currentStage.stageName(),
                            project.getWorkflowTemplateId(),
                            guidanceGovernanceSummary,
                            null,
                            null,
                            null,
                            budgetSummary,
                            null,
                            List.of(),
                            List.of(),
                            List.of()
                    )
            );

            ProjectCenterListVO vo = new ProjectCenterListVO();
            vo.setId(project.getId());
            vo.setProjectCode(project.getProjectCode());
            vo.setProjectName(project.getProjectName());
            vo.setProjectType(project.getProjectType());
            vo.setStatus(project.getStatus());
            vo.setGovernanceStatus(governanceState == null ? null : governanceState.getGovernanceStatus());
            vo.setBlockedFlag(governanceState == null ? 0 : governanceState.getBlockedFlag());
            vo.setAtRiskFlag(governanceState == null ? 0 : governanceState.getAtRiskFlag());
            vo.setRiskLevel(project.getRiskLevel());
            vo.setOwnerDisplayName(ownerDisplayMap.getOrDefault(project.getOwnerUserId(), project.getOwnerUserId()));
            vo.setCurrentStageCode(firstNonBlank(
                    governanceState == null ? null : governanceState.getCurrentStageCode(),
                    currentStage.stageCode()));
            vo.setCurrentStageName(firstNonBlank(
                    governanceState == null ? null : governanceState.getCurrentStageName(),
                    currentStage.stageName()));
            vo.setCurrentStageStatus(firstNonBlank(
                    governanceState == null ? null : governanceState.getCurrentStageStatus(),
                    currentStage.stageStatus()));
            vo.setCurrentGateStatus(firstNonBlank(
                    governanceState == null ? null : governanceState.getCurrentGateStatus(),
                    currentStage.gateStatus()));
            vo.setBudgetStatus(firstNonBlank(
                    governanceState == null ? null : governanceState.getBudgetStatus(),
                    budgetSummary.getStatus()));
            vo.setBudgetApprovedAmount(budgetSummary.getApprovedAmount());
            vo.setBudgetConsumedAmount(budgetSummary.getConsumedAmount());
            vo.setBudgetRemainingAmount(budgetSummary.getRemainingAmount());
            vo.setPendingDecisionItemCount(firstNonNull(
                    governanceState == null ? null : governanceState.getPendingDecisionCount(),
                    countPendingDecisions(decisionsByProject.get(project.getId()))));
            vo.setBlockerDecisionItemCount(firstNonNull(
                    governanceState == null ? null : governanceState.getBlockerDecisionCount(),
                    countBlockerPendingDecisions(decisionsByProject.get(project.getId()))));
            vo.setBlockerReasonSummary(governanceState == null ? null : governanceState.getBlockerReasonSummary());
            vo.setRecommendedNextStep(nextStepGuidance == null ? null : nextStepGuidance.getRecommendedNextStep());
            vo.setRecommendedActorRole(nextStepGuidance == null ? null : nextStepGuidance.getRecommendedActorRole());
            vo.setRecommendedPriority(nextStepGuidance == null ? null : nextStepGuidance.getRecommendedPriority());
            vo.setUpdateTime(firstNonNull(project.getUpdateTime(), project.getCreateTime()));
            return vo;
        }).toList();

        result.setRecords(records);
        return result;
    }

    @Override
    public ProjectCenterDetailVO getProjectCenterDetail(String id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        ProjectGovernanceState governanceState = projectGovernanceLinkageService.getOrRecomputeProjectState(id);

        List<ProjectStage> stages = projectStageService.list(new LambdaQueryWrapper<ProjectStage>()
                .eq(ProjectStage::getProjectId, id)
                .orderByAsc(ProjectStage::getStageOrder)
                .orderByAsc(ProjectStage::getCreateTime));
        RequirementIntake requirementIntake = requirementIntakeService.getByProjectId(id);
        List<ClarificationItem> clarificationItems = clarificationItemService.list(new LambdaQueryWrapper<ClarificationItem>()
                .eq(ClarificationItem::getProjectId, id)
                .orderByDesc(ClarificationItem::getUpdateTime)
                .orderByDesc(ClarificationItem::getCreateTime));
        List<DecisionItem> decisionItems = decisionItemService.list(new LambdaQueryWrapper<DecisionItem>()
                .eq(DecisionItem::getProjectId, id)
                .orderByDesc(DecisionItem::getUpdateTime)
                .orderByDesc(DecisionItem::getCreateTime));
        List<ApprovalRecord> approvalRecords = approvalRecordService.list(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getProjectId, id)
                .orderByDesc(ApprovalRecord::getDecidedAt)
                .orderByDesc(ApprovalRecord::getSubmittedAt)
                .orderByDesc(ApprovalRecord::getCreateTime));
        List<BudgetPlan> budgetPlans = budgetPlanService.list(new LambdaQueryWrapper<BudgetPlan>()
                .eq(BudgetPlan::getProjectId, id)
                .orderByDesc(BudgetPlan::getEffectiveAt)
                .orderByDesc(BudgetPlan::getUpdateTime)
                .orderByDesc(BudgetPlan::getCreateTime));
        List<BudgetLedger> budgetLedgers = budgetLedgerService.list(new LambdaQueryWrapper<BudgetLedger>()
                .eq(BudgetLedger::getProjectId, id)
                .orderByDesc(BudgetLedger::getOccurredAt)
                .orderByDesc(BudgetLedger::getCreateTime));
        List<MeetingRecord> meetingRecords = meetingRecordService.listByProjectId(id);
        List<ProjectToolBinding> projectToolBindings = projectToolBindingService.listByProjectId(id);
        List<ToolIntegrationAudit> toolIntegrationAudits = toolIntegrationAuditService.listByProjectId(id, DETAIL_ITEM_LIMIT);

        BudgetPlan latestBudgetPlan = budgetPlans.isEmpty() ? null : budgetPlans.get(0);
        ProjectBudgetSummaryVO budgetSummary = buildBudgetSummary(latestBudgetPlan);
        ProjectDecisionSummaryVO decisionSummary = buildDecisionSummary(decisionItems);
        ProjectApprovalSummaryVO approvalSummary = buildApprovalSummary(approvalRecords);
        ProjectRequirementSummaryVO requirementSummary = buildRequirementSummary(requirementIntake, clarificationItems);
        ResolvedStage currentStage = resolveCurrentStage(project, stages);
        WorkflowTemplate workflowTemplate = resolveWorkflowTemplate(project.getWorkflowTemplateId());
        List<WorkflowTemplateStage> workflowTemplateStages = workflowTemplate == null
                ? List.of()
                : listWorkflowTemplateStages(workflowTemplate.getId());
        WorkflowTemplateStage currentWorkflowTemplateStage = resolveWorkflowTemplateStage(
                project.getCurrentStageCode(),
                workflowTemplateStages
        );
        List<String> relevantStageCodes = resolveRelevantStageCodes(stages, workflowTemplateStages, project.getCurrentStageCode());
        RoleResolution roleResolution = resolveProjectRoles(relevantStageCodes, project.getCurrentStageCode());
        List<ProjectGateConditionVO> gateConditions = buildGateConditions(
                project,
                currentStage,
                workflowTemplate,
                currentWorkflowTemplateStage,
                decisionSummary,
                approvalSummary,
                budgetSummary
        );

        ProjectGovernanceSummaryVO governanceSummary = new ProjectGovernanceSummaryVO();
        governanceSummary.setCurrentStageCode(firstNonBlank(
                governanceState == null ? null : governanceState.getCurrentStageCode(),
                currentStage.stageCode()));
        governanceSummary.setCurrentStageName(firstNonBlank(
                governanceState == null ? null : governanceState.getCurrentStageName(),
                currentStage.stageName()));
        governanceSummary.setCurrentStageStatus(firstNonBlank(
                governanceState == null ? null : governanceState.getCurrentStageStatus(),
                currentStage.stageStatus()));
        governanceSummary.setCurrentGateStatus(firstNonBlank(
                governanceState == null ? null : governanceState.getCurrentGateStatus(),
                currentStage.gateStatus()));
        governanceSummary.setGovernanceStatus(governanceState == null ? null : governanceState.getGovernanceStatus());
        governanceSummary.setBlockedFlag(governanceState == null ? 0 : governanceState.getBlockedFlag());
        governanceSummary.setAtRiskFlag(governanceState == null ? 0 : governanceState.getAtRiskFlag());
        governanceSummary.setPendingDecisionItemCount(firstNonNull(
                governanceState == null ? null : governanceState.getPendingDecisionCount(),
                decisionSummary.getPendingCount()));
        governanceSummary.setPendingApprovalCount(firstNonNull(
                governanceState == null ? null : governanceState.getPendingApprovalCount(),
                approvalSummary.getPendingCount()));
        governanceSummary.setBlockerApprovalCount(firstNonNull(
                governanceState == null ? null : governanceState.getBlockerApprovalCount(),
                approvalSummary.getBlockerCount()));
        governanceSummary.setBlockerDecisionCount(firstNonNull(
                governanceState == null ? null : governanceState.getBlockerDecisionCount(),
                decisionSummary.getBlockerCount()));
        governanceSummary.setBlockingGateConditionCount(firstNonNull(
                governanceState == null ? null : governanceState.getBlockingGateConditionCount(),
                countBlockedGateConditions(gateConditions)));
        governanceSummary.setFailedGateConditionCount(firstNonNull(
                governanceState == null ? null : governanceState.getFailedGateConditionCount(),
                countBlockedGateConditions(gateConditions)));
        governanceSummary.setMissingCriticalRoleCount(firstNonNull(
                governanceState == null ? null : governanceState.getMissingCriticalRoleCount(),
                roleResolution.missingCriticalRoles().size()));
        governanceSummary.setBudgetStatus(firstNonBlank(
                governanceState == null ? null : governanceState.getBudgetStatus(),
                budgetSummary.getStatus()));
        governanceSummary.setRequirementCompletenessScore(requirementSummary.getCompletenessScore());
        governanceSummary.setClarificationCount(firstNonNull(
                governanceState == null ? null : governanceState.getClarificationCount(),
                requirementSummary.getClarificationCount()));
        governanceSummary.setBlockerClarificationCount(firstNonNull(
                governanceState == null ? null : governanceState.getBlockerClarificationCount(),
                requirementSummary.getBlockerCount()));
        governanceSummary.setBlockerReasonSummary(firstNonBlank(
                governanceState == null ? null : governanceState.getBlockerReasonSummary(),
                buildFallbackGovernanceReason(governanceState, requirementSummary, decisionSummary, approvalSummary, budgetSummary, roleResolution.missingCriticalRoles(), gateConditions)));
        governanceSummary.setLastRecomputedAt(governanceState == null ? null : governanceState.getLastRecomputedAt());
        ProjectCollaborationSummaryVO collaborationSummary = buildCollaborationSummary(
                clarificationItems,
                decisionItems,
                meetingRecords);
        ProjectNextStepGuidanceVO nextStepGuidance = nextStepGuidanceResolver.resolve(
                new ProjectNextStepGuidanceResolver.ProjectNextStepContext(
                        governanceSummary.getCurrentStageCode(),
                        governanceSummary.getCurrentStageName(),
                        project.getWorkflowTemplateId(),
                        governanceSummary,
                        requirementSummary,
                        decisionSummary,
                        approvalSummary,
                        budgetSummary,
                        collaborationSummary,
                        gateConditions,
                        roleResolution.missingCriticalRoles(),
                        roleResolution.currentStageRecommendedRoles()
                )
        );

        ProjectCenterDetailVO detail = new ProjectCenterDetailVO();
        detail.setProject(projectConvert.toVO(project));
        detail.setOwnerDisplayName(resolveUserDisplayName(project.getOwnerUserId()));
        detail.setWorkflowTemplateName(resolveWorkflowTemplateName(workflowTemplate));
        detail.setWorkflowTemplateCurrentStageCode(
                currentWorkflowTemplateStage == null ? null : currentWorkflowTemplateStage.getStageCode()
        );
        detail.setWorkflowTemplateCurrentStageName(
                currentWorkflowTemplateStage == null ? null : currentWorkflowTemplateStage.getStageName()
        );
        detail.setGovernanceSummary(governanceSummary);
        detail.setDecisionSummary(decisionSummary);
        detail.setApprovalSummary(approvalSummary);
        detail.setBudgetSummary(budgetSummary);
        detail.setRequirementSummary(requirementSummary);
        detail.setCollaborationSummary(collaborationSummary);
        detail.setNextStepGuidance(nextStepGuidance);
        detail.setRequirementIntake(requirementIntakeConvert.toVO(requirementIntake));
        detail.setStages(projectStageConvert.toVOs(stages));
        detail.setRecentClarificationItems(clarificationItemConvert.toVOs(limitList(clarificationItems, DETAIL_ITEM_LIMIT)));
        detail.setRecentDecisionItems(decisionItemConvert.toVOs(limitList(decisionItems, DETAIL_ITEM_LIMIT)));
        detail.setRecentApprovalRecords(approvalRecordConvert.toVOs(limitList(approvalRecords, DETAIL_ITEM_LIMIT)));
        detail.setRecentBudgetLedgerEntries(buildBudgetLedgerVOs(limitList(budgetLedgers, DETAIL_ITEM_LIMIT)));
        detail.setRecentMeetingRecords(meetingRecordConvert.toVOs(limitList(meetingRecords, DETAIL_ITEM_LIMIT)));
        detail.setProjectToolBindings(projectToolBindingConvert.toVOs(limitList(projectToolBindings, DETAIL_ITEM_LIMIT)));
        detail.setRecentToolIntegrationAudits(toolIntegrationAuditConvert.toVOs(toolIntegrationAudits));
        detail.setLinkedAgentRoles(roleResolution.linkedRoles());
        detail.setCurrentStageRecommendedRoles(roleResolution.currentStageRecommendedRoles());
        detail.setMissingCriticalRoles(roleResolution.missingCriticalRoles());
        detail.setCurrentGateConditions(gateConditions);
        detail.setRecentActivities(buildActivities(
                project,
                requirementIntake,
                clarificationItems,
                stages,
                decisionItems,
                approvalRecords,
                budgetLedgers,
                meetingRecords,
                toolIntegrationAudits));
        return detail;
    }

    private ProjectGovernanceSummaryVO buildListGovernanceSummary(
            ProjectGovernanceState governanceState,
            ResolvedStage currentStage,
            ProjectBudgetSummaryVO budgetSummary,
            Integer pendingDecisionCount,
            Integer blockerDecisionCount) {
        ProjectGovernanceSummaryVO governanceSummary = new ProjectGovernanceSummaryVO();
        governanceSummary.setCurrentStageCode(firstNonBlank(
                governanceState == null ? null : governanceState.getCurrentStageCode(),
                currentStage.stageCode()));
        governanceSummary.setCurrentStageName(firstNonBlank(
                governanceState == null ? null : governanceState.getCurrentStageName(),
                currentStage.stageName()));
        governanceSummary.setCurrentStageStatus(firstNonBlank(
                governanceState == null ? null : governanceState.getCurrentStageStatus(),
                currentStage.stageStatus()));
        governanceSummary.setCurrentGateStatus(firstNonBlank(
                governanceState == null ? null : governanceState.getCurrentGateStatus(),
                currentStage.gateStatus()));
        governanceSummary.setGovernanceStatus(governanceState == null ? null : governanceState.getGovernanceStatus());
        governanceSummary.setBlockedFlag(governanceState == null ? 0 : governanceState.getBlockedFlag());
        governanceSummary.setAtRiskFlag(governanceState == null ? 0 : governanceState.getAtRiskFlag());
        governanceSummary.setPendingDecisionItemCount(firstNonNull(
                governanceState == null ? null : governanceState.getPendingDecisionCount(),
                pendingDecisionCount));
        governanceSummary.setBlockerDecisionCount(firstNonNull(
                governanceState == null ? null : governanceState.getBlockerDecisionCount(),
                blockerDecisionCount));
        governanceSummary.setPendingApprovalCount(governanceState == null ? null : governanceState.getPendingApprovalCount());
        governanceSummary.setBlockerApprovalCount(governanceState == null ? null : governanceState.getBlockerApprovalCount());
        governanceSummary.setBlockingGateConditionCount(governanceState == null ? null : governanceState.getBlockingGateConditionCount());
        governanceSummary.setFailedGateConditionCount(governanceState == null ? null : governanceState.getFailedGateConditionCount());
        governanceSummary.setMissingCriticalRoleCount(governanceState == null ? null : governanceState.getMissingCriticalRoleCount());
        governanceSummary.setBlockerClarificationCount(governanceState == null ? null : governanceState.getBlockerClarificationCount());
        governanceSummary.setBudgetStatus(firstNonBlank(
                governanceState == null ? null : governanceState.getBudgetStatus(),
                budgetSummary == null ? null : budgetSummary.getStatus()));
        governanceSummary.setBlockerReasonSummary(governanceState == null ? null : governanceState.getBlockerReasonSummary());
        governanceSummary.setLastRecomputedAt(governanceState == null ? null : governanceState.getLastRecomputedAt());
        return governanceSummary;
    }

    private Map<String, List<ProjectStage>> loadStagesByProject(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        return projectStageService.list(new LambdaQueryWrapper<ProjectStage>()
                        .in(ProjectStage::getProjectId, projectIds)
                        .orderByAsc(ProjectStage::getStageOrder)
                        .orderByAsc(ProjectStage::getCreateTime))
                .stream()
                .collect(Collectors.groupingBy(ProjectStage::getProjectId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, List<DecisionItem>> loadDecisionsByProject(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        return decisionItemService.list(new LambdaQueryWrapper<DecisionItem>()
                        .in(DecisionItem::getProjectId, projectIds)
                        .orderByDesc(DecisionItem::getUpdateTime)
                        .orderByDesc(DecisionItem::getCreateTime))
                .stream()
                .collect(Collectors.groupingBy(DecisionItem::getProjectId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, BudgetPlan> loadLatestBudgetPlans(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        Map<String, List<BudgetPlan>> grouped = budgetPlanService.list(new LambdaQueryWrapper<BudgetPlan>()
                        .in(BudgetPlan::getProjectId, projectIds)
                        .orderByDesc(BudgetPlan::getEffectiveAt)
                        .orderByDesc(BudgetPlan::getUpdateTime)
                        .orderByDesc(BudgetPlan::getCreateTime))
                .stream()
                .collect(Collectors.groupingBy(BudgetPlan::getProjectId, LinkedHashMap::new, Collectors.toList()));

        Map<String, BudgetPlan> result = new LinkedHashMap<>();
        grouped.forEach((projectId, plans) -> result.put(projectId, plans.isEmpty() ? null : plans.get(0)));
        return result;
    }

    private Map<String, String> loadUserDisplayMap(Collection<String> userIds) {
        List<String> resolvedUserIds = userIds == null
                ? List.of()
                : userIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
        if (resolvedUserIds.isEmpty()) {
            return Map.of();
        }

        return userService.listByIds(resolvedUserIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        this::buildUserDisplayName,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private String resolveUserDisplayName(String userId) {
        if (userId == null || userId.isBlank()) {
            return "-";
        }
        return Optional.ofNullable(userService.getById(userId))
                .map(this::buildUserDisplayName)
                .orElse(userId);
    }

    private WorkflowTemplate resolveWorkflowTemplate(String workflowTemplateId) {
        if (workflowTemplateId == null || workflowTemplateId.isBlank()) {
            return null;
        }
        return workflowTemplateService.getById(workflowTemplateId);
    }

    private String resolveWorkflowTemplateName(WorkflowTemplate workflowTemplate) {
        if (workflowTemplate == null) {
            return "-";
        }
        return firstNonBlank(
                workflowTemplate.getTemplateName(),
                workflowTemplate.getTemplateCode(),
                workflowTemplate.getId()
        );
    }

    private String buildUserDisplayName(User user) {
        if (user == null) {
            return "-";
        }
        if (user.getRealname() != null && !user.getRealname().isBlank()) {
            if (user.getUsername() != null
                    && !user.getUsername().isBlank()
                    && !Objects.equals(user.getRealname(), user.getUsername())) {
                return user.getRealname() + " (" + user.getUsername() + ")";
            }
            return user.getRealname();
        }
        return firstNonBlank(user.getUsername(), user.getId(), "-");
    }

    private ResolvedStage resolveCurrentStage(Project project, List<ProjectStage> stages) {
        List<ProjectStage> safeStages = stages == null ? List.of() : stages;
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
                        .comparing((ProjectStage stage) -> Optional.ofNullable(stage.getStageOrder()).orElse(0))
                        .thenComparing(stage -> firstNonNull(stage.getUpdateTime(), stage.getCreateTime()),
                                Comparator.nullsLast(LocalDateTime::compareTo)))
                .orElse(null);

        ProjectStage resolved = firstNonNull(activeStage, matchedStage, fallbackStage);
        String stageCode = resolved != null ? resolved.getStageCode() : project.getCurrentStageCode();
        String stageName = resolved != null
                ? firstNonBlank(resolved.getStageName(), lifecycleStageLabel(stageCode), stageCode)
                : firstNonBlank(lifecycleStageLabel(stageCode), stageCode, "-");
        String stageStatus = resolved != null ? resolved.getStageStatus() : null;
        String gateStatus = resolved != null ? resolved.getGateStatus() : null;
        return new ResolvedStage(stageCode, stageName, stageStatus, gateStatus);
    }

    private List<WorkflowTemplateStage> listWorkflowTemplateStages(String workflowTemplateId) {
        if (workflowTemplateId == null || workflowTemplateId.isBlank()) {
            return List.of();
        }
        return workflowTemplateStageService.list(new LambdaQueryWrapper<WorkflowTemplateStage>()
                .eq(WorkflowTemplateStage::getTemplateId, workflowTemplateId)
                .orderByAsc(WorkflowTemplateStage::getStageOrder)
                .orderByAsc(WorkflowTemplateStage::getCreateTime));
    }

    private WorkflowTemplateStage resolveWorkflowTemplateStage(
            String currentStageCode,
            List<WorkflowTemplateStage> workflowTemplateStages) {
        List<WorkflowTemplateStage> stages = workflowTemplateStages == null ? List.of() : workflowTemplateStages;
        return stages.stream()
                .filter(stage -> Objects.equals(stage.getStageCode(), currentStageCode))
                .findFirst()
                .orElseGet(() -> stages.stream()
                        .filter(stage -> Objects.equals(stage.getEnabledFlag(), 1))
                        .findFirst()
                        .orElse(stages.stream().findFirst().orElse(null)));
    }

    private List<String> resolveRelevantStageCodes(
            List<ProjectStage> projectStages,
            List<WorkflowTemplateStage> workflowTemplateStages,
            String currentStageCode) {
        List<String> fromTemplate = safe(workflowTemplateStages).stream()
                .filter(stage -> Objects.equals(stage.getEnabledFlag(), 1))
                .map(WorkflowTemplateStage::getStageCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!fromTemplate.isEmpty()) {
            return fromTemplate;
        }

        List<String> fromProject = safe(projectStages).stream()
                .map(ProjectStage::getStageCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!fromProject.isEmpty()) {
            return fromProject;
        }

        if (currentStageCode != null && !currentStageCode.isBlank()) {
            return List.of(currentStageCode);
        }
        return List.of();
    }

    private RoleResolution resolveProjectRoles(List<String> relevantStageCodes, String currentStageCode) {
        List<AgentRole> enabledRoles = agentRoleService.list(new LambdaQueryWrapper<AgentRole>()
                .eq(AgentRole::getStatus, AgentRoleStatus.ACTIVE.getCode())
                .orderByDesc(AgentRole::getDefaultFlag)
                .orderByAsc(AgentRole::getRoleCategory)
                .orderByAsc(AgentRole::getRoleName));
        if (enabledRoles.isEmpty()) {
            return new RoleResolution(List.of(), List.of(), List.of("当前项目没有可用的启用 Agent 角色"));
        }

        List<String> roleIds = enabledRoles.stream().map(AgentRole::getId).filter(Objects::nonNull).toList();
        Map<String, List<AgentRoleStageParticipation>> participationsByRole = loadAgentRoleParticipations(roleIds);
        Map<String, List<AgentRoleAllowedAction>> actionsByRole = loadAgentRoleActions(roleIds);

        List<ProjectAgentRoleSummaryVO> linkedRoles = enabledRoles.stream()
                .filter(role -> participatesInRelevantStages(
                        participationsByRole.get(role.getId()),
                        relevantStageCodes,
                        currentStageCode))
                .map(role -> toProjectAgentRoleSummaryVO(
                        role,
                        participationsByRole.get(role.getId()),
                        actionsByRole.get(role.getId()),
                        currentStageCode))
                .toList();

        List<ProjectAgentRoleSummaryVO> currentStageRecommendedRoles = linkedRoles.stream()
                .filter(role -> Objects.equals(role.getCurrentStageParticipationType(), AgentStageParticipationType.REQUIRED.getCode())
                        || Objects.equals(role.getCurrentStageParticipationType(), AgentStageParticipationType.OPTIONAL.getCode()))
                .sorted((left, right) -> {
                    int priorityCompare = Integer.compare(
                            participationPriority(left.getCurrentStageParticipationType()),
                            participationPriority(right.getCurrentStageParticipationType()));
                    if (priorityCompare != 0) {
                        return priorityCompare;
                    }
                    return firstNonBlank(left.getRoleName(), "").compareTo(firstNonBlank(right.getRoleName(), ""));
                })
                .toList();

        List<String> missingCriticalRoles = new ArrayList<>();
        boolean hasRequiredRole = currentStageRecommendedRoles.stream()
                .anyMatch(role -> Objects.equals(role.getCurrentStageParticipationType(), AgentStageParticipationType.REQUIRED.getCode()));
        if (!hasRequiredRole) {
            missingCriticalRoles.add("当前阶段未配置必需的 Agent 角色");
        }
        if (linkedRoles.isEmpty()) {
            missingCriticalRoles.add("当前项目没有与流程阶段匹配的 Agent 角色");
        }
        return new RoleResolution(linkedRoles, currentStageRecommendedRoles, missingCriticalRoles);
    }

    private Map<String, List<AgentRoleStageParticipation>> loadAgentRoleParticipations(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        return agentRoleStageParticipationService.list(new LambdaQueryWrapper<AgentRoleStageParticipation>()
                        .in(AgentRoleStageParticipation::getAgentRoleId, roleIds)
                        .orderByAsc(AgentRoleStageParticipation::getStageCode))
                .stream()
                .collect(Collectors.groupingBy(
                        AgentRoleStageParticipation::getAgentRoleId,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    private Map<String, List<AgentRoleAllowedAction>> loadAgentRoleActions(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        return agentRoleAllowedActionService.list(new LambdaQueryWrapper<AgentRoleAllowedAction>()
                        .in(AgentRoleAllowedAction::getAgentRoleId, roleIds)
                        .orderByAsc(AgentRoleAllowedAction::getActionCode))
                .stream()
                .collect(Collectors.groupingBy(
                        AgentRoleAllowedAction::getAgentRoleId,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    private boolean participatesInRelevantStages(
            List<AgentRoleStageParticipation> participations,
            List<String> relevantStageCodes,
            String currentStageCode) {
        List<AgentRoleStageParticipation> items = safe(participations);
        if (!items.isEmpty()) {
            return items.stream()
                    .filter(item -> !Objects.equals(item.getParticipationType(), AgentStageParticipationType.NOT_INVOLVED.getCode()))
                    .anyMatch(item -> relevantStageCodes.contains(item.getStageCode())
                            || Objects.equals(item.getStageCode(), currentStageCode));
        }
        return false;
    }

    private ProjectAgentRoleSummaryVO toProjectAgentRoleSummaryVO(
            AgentRole role,
            List<AgentRoleStageParticipation> participations,
            List<AgentRoleAllowedAction> allowedActions,
            String currentStageCode) {
        ProjectAgentRoleSummaryVO vo = new ProjectAgentRoleSummaryVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCategory(role.getRoleCategory());
        vo.setStatus(role.getStatus());
        vo.setDefaultFlag(role.getDefaultFlag());
        vo.setBudgetFactor(role.getBudgetFactor());
        vo.setApprovalCollaborationFlag(role.getApprovalCollaborationFlag());
        vo.setCurrentStageParticipationType(resolveCurrentStageParticipationType(participations, currentStageCode));
        vo.setAllowedActionCodes(safe(allowedActions).stream()
                .filter(action -> Objects.equals(action.getAllowedFlag(), 1))
                .map(AgentRoleAllowedAction::getActionCode)
                .toList());
        return vo;
    }

    private String resolveCurrentStageParticipationType(
            List<AgentRoleStageParticipation> participations,
            String currentStageCode) {
        if (currentStageCode == null || currentStageCode.isBlank()) {
            return null;
        }
        return safe(participations).stream()
                .filter(item -> Objects.equals(item.getStageCode(), currentStageCode))
                .map(AgentRoleStageParticipation::getParticipationType)
                .findFirst()
                .orElse(AgentStageParticipationType.NOT_INVOLVED.getCode());
    }

    private int participationPriority(String participationType) {
        if (Objects.equals(participationType, AgentStageParticipationType.REQUIRED.getCode())) {
            return 0;
        }
        if (Objects.equals(participationType, AgentStageParticipationType.OPTIONAL.getCode())) {
            return 1;
        }
        return 2;
    }

    private List<ProjectGateConditionVO> buildGateConditions(
            Project project,
            ResolvedStage currentStage,
            WorkflowTemplate workflowTemplate,
            WorkflowTemplateStage currentWorkflowTemplateStage,
            ProjectDecisionSummaryVO decisionSummary,
            ProjectApprovalSummaryVO approvalSummary,
            ProjectBudgetSummaryVO budgetSummary) {
        if (workflowTemplate == null) {
            return List.of();
        }

        List<ProjectGateConditionVO> items = new ArrayList<>();
        addGateCondition(
                items,
                "gate_checks",
                "Gate 检查",
                workflowTemplate.getGateChecksConfig(),
                resolveGateCheckStatus(currentStage),
                currentWorkflowTemplateStage == null
                        ? "当前模板未配置匹配阶段"
                        : "当前模板阶段：" + firstNonBlank(
                                currentWorkflowTemplateStage.getStageName(),
                                lifecycleStageLabel(currentWorkflowTemplateStage.getStageCode()),
                                currentWorkflowTemplateStage.getStageCode(),
                                "-"
                        )
        );
        addGateCondition(
                items,
                "blocking_decision",
                "阻塞决策条件",
                workflowTemplate.getBlockingDecisionConfig(),
                decisionSummary.getBlockerCount() > 0
                        ? "blocked"
                        : decisionSummary.getPendingCount() > 0 ? "warning" : "pass",
                "待处理决策 " + zeroIfNull(decisionSummary.getPendingCount())
                        + "，阻塞决策 " + zeroIfNull(decisionSummary.getBlockerCount())
        );
        addGateCondition(
                items,
                "blocking_approval",
                "阻塞审批条件",
                workflowTemplate.getBlockingApprovalConfig(),
                approvalSummary.getBlockerCount() > 0
                        ? "blocked"
                        : approvalSummary.getPendingCount() > 0 ? "warning" : "pass",
                "待处理审批 " + zeroIfNull(approvalSummary.getPendingCount())
                        + "，阻塞审批 " + zeroIfNull(approvalSummary.getBlockerCount())
        );
        addGateCondition(
                items,
                "budget_threshold",
                "预算阈值条件",
                workflowTemplate.getBudgetThresholdConfig(),
                resolveBudgetGateStatus(budgetSummary.getStatus()),
                "预算健康状态：" + firstNonBlank(budgetSummary.getStatus(), "unplanned")
        );
        addGateCondition(
                items,
                "high_risk_approval",
                "高风险动作审批要求",
                workflowTemplate.getHighRiskApprovalConfig(),
                resolveHighRiskGateStatus(project, approvalSummary),
                "项目风险等级：" + firstNonBlank(project.getRiskLevel(), "-")
        );
        return items;
    }

    private void addGateCondition(
            List<ProjectGateConditionVO> items,
            String key,
            String title,
            String configValue,
            String status,
            String summary) {
        if (configValue == null || configValue.isBlank()) {
            return;
        }
        ProjectGateConditionVO item = new ProjectGateConditionVO();
        item.setKey(key);
        item.setTitle(title);
        item.setStatus(status);
        item.setSummary(summary);
        items.add(item);
    }

    private String resolveGateCheckStatus(ResolvedStage currentStage) {
        if (currentStage == null) {
            return "configured";
        }
        if (Objects.equals(currentStage.gateStatus(), "rejected")) {
            return "blocked";
        }
        if (Objects.equals(currentStage.gateStatus(), "pending")) {
            return "warning";
        }
        if (Objects.equals(currentStage.gateStatus(), "approved")
                || Objects.equals(currentStage.gateStatus(), "not_required")) {
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

    private String resolveHighRiskGateStatus(Project project, ProjectApprovalSummaryVO approvalSummary) {
        boolean highRisk = Objects.equals(project.getRiskLevel(), "high")
                || Objects.equals(project.getRiskLevel(), "critical");
        if (!highRisk) {
            return "pass";
        }
        if (approvalSummary.getBlockerCount() > 0) {
            return "blocked";
        }
        if (approvalSummary.getPendingCount() > 0) {
            return "warning";
        }
        return "warning";
    }

    private int countBlockedGateConditions(List<ProjectGateConditionVO> gateConditions) {
        return (int) (gateConditions == null ? List.<ProjectGateConditionVO>of() : gateConditions).stream()
                .filter(Objects::nonNull)
                .filter(condition -> Objects.equals(condition.getStatus(), "blocked"))
                .count();
    }

    private String buildFallbackGovernanceReason(
            ProjectGovernanceState governanceState,
            ProjectRequirementSummaryVO requirementSummary,
            ProjectDecisionSummaryVO decisionSummary,
            ProjectApprovalSummaryVO approvalSummary,
            ProjectBudgetSummaryVO budgetSummary,
            List<String> missingCriticalRoles,
            List<ProjectGateConditionVO> gateConditions) {
        if (governanceState != null && governanceState.getBlockerReasonSummary() != null
                && !governanceState.getBlockerReasonSummary().isBlank()) {
            return governanceState.getBlockerReasonSummary();
        }
        List<String> reasons = new ArrayList<>();
        if (zeroIfNull(requirementSummary.getBlockerCount()) > 0) {
            reasons.add("阻塞澄清项");
        }
        if (zeroIfNull(decisionSummary.getBlockerCount()) > 0) {
            reasons.add("阻塞决策");
        }
        if (zeroIfNull(approvalSummary.getBlockerCount()) > 0) {
            reasons.add("阻塞审批");
        }
        if (Objects.equals(budgetSummary.getStatus(), BudgetHealthStatus.OVERRUN.getCode())) {
            reasons.add("预算超支");
        } else if (Objects.equals(budgetSummary.getStatus(), BudgetHealthStatus.WARNING.getCode())) {
            reasons.add("预算预警");
        }
        if (missingCriticalRoles != null && !missingCriticalRoles.isEmpty()) {
            reasons.add("关键角色缺失");
        }
        if (countBlockedGateConditions(gateConditions) > 0) {
            reasons.add("门禁条件阻塞");
        }
        return reasons.isEmpty() ? "治理状态正常" : reasons.stream().limit(3).collect(Collectors.joining(" / "));
    }

    private ProjectDecisionSummaryVO buildDecisionSummary(List<DecisionItem> decisionItems) {
        List<DecisionItem> items = decisionItems == null ? List.of() : decisionItems;
        int openCount = (int) items.stream()
                .filter(item -> Objects.equals(item.getStatus(), DecisionItemStatus.OPEN.getCode())
                        || Objects.equals(item.getStatus(), DecisionItemStatus.PENDING_APPROVAL.getCode()))
                .count();
        int blockerCount = (int) items.stream()
                .filter(item -> Objects.equals(item.getBlockerFlag(), 1))
                .filter(item -> Objects.equals(item.getStatus(), DecisionItemStatus.OPEN.getCode())
                        || Objects.equals(item.getStatus(), DecisionItemStatus.PENDING_APPROVAL.getCode()))
                .count();

        ProjectDecisionSummaryVO summary = new ProjectDecisionSummaryVO();
        summary.setOpenCount(openCount);
        summary.setPendingCount(openCount);
        summary.setBlockerCount(blockerCount);
        summary.setResolvedCount(Math.max(items.size() - summary.getPendingCount(), 0));
        return summary;
    }

    private ProjectApprovalSummaryVO buildApprovalSummary(List<ApprovalRecord> approvalRecords) {
        List<ApprovalRecord> records = approvalRecords == null ? List.of() : approvalRecords;

        ProjectApprovalSummaryVO summary = new ProjectApprovalSummaryVO();
        summary.setPendingCount((int) records.stream()
                .filter(this::isPendingApproval)
                .count());
        summary.setBlockerCount((int) records.stream()
                .filter(this::isPendingApproval)
                .filter(record -> Objects.equals(record.getBlockerFlag(), 1))
                .count());
        summary.setApprovedCount((int) records.stream()
                .filter(record -> Objects.equals(record.getApprovalStatus(), ApprovalStatus.APPROVED.getCode()))
                .count());
        summary.setRejectedCount((int) records.stream()
                .filter(record -> Objects.equals(record.getApprovalStatus(), ApprovalStatus.REJECTED.getCode()))
                .count());
        return summary;
    }

    private ProjectBudgetSummaryVO buildBudgetSummary(BudgetPlan budgetPlan) {
        ProjectBudgetSummaryVO summary = new ProjectBudgetSummaryVO();
        if (budgetPlan == null) {
            summary.setStatus(BudgetHealthStatus.UNPLANNED.getCode());
            summary.setCurrencyCode("TOKEN");
            summary.setTotalBudgetAmount(0L);
            summary.setLockedAmount(0L);
            summary.setConsumedAmount(0L);
            summary.setPendingIncreaseAmount(0L);
            summary.setRemainingAmount(0L);
            return summary;
        }

        long proposedAmount = zeroIfNull(budgetPlan.getProposedAmount());
        long approvedAmount = zeroIfNull(budgetPlan.getApprovedAmount());
        long reservedAmount = zeroIfNull(budgetPlan.getReservedAmount());
        long consumedAmount = zeroIfNull(budgetPlan.getConsumedAmount());
        long totalBudgetAmount = approvedAmount > 0L ? approvedAmount : proposedAmount;
        long pendingIncreaseAmount = Math.max(proposedAmount - approvedAmount, 0L);
        long remainingAmount = totalBudgetAmount - consumedAmount;

        summary.setPlanName(budgetPlan.getPlanName());
        summary.setCurrencyCode(firstNonBlank(budgetPlan.getCurrencyCode(), "TOKEN"));
        summary.setProposedAmount(proposedAmount);
        summary.setApprovedAmount(approvedAmount);
        summary.setReservedAmount(reservedAmount);
        summary.setLockedAmount(reservedAmount);
        summary.setConsumedAmount(consumedAmount);
        summary.setTotalBudgetAmount(totalBudgetAmount);
        summary.setPendingIncreaseAmount(pendingIncreaseAmount);
        summary.setRemainingAmount(remainingAmount);
        summary.setEffectiveAt(budgetPlan.getEffectiveAt());
        summary.setLastUpdatedAt(firstNonNull(
                budgetPlan.getUpdateTime(),
                budgetPlan.getEffectiveAt(),
                budgetPlan.getCreateTime()));

        if (Objects.equals(budgetPlan.getStatus(), BudgetPlanStatus.DRAFT.getCode())
                || Objects.equals(budgetPlan.getStatus(), BudgetPlanStatus.PENDING_APPROVAL.getCode())) {
            summary.setStatus(BudgetHealthStatus.PENDING.getCode());
            return summary;
        }
        if (approvedAmount <= 0L) {
            summary.setStatus(BudgetHealthStatus.PENDING.getCode());
            return summary;
        }
        if (consumedAmount >= approvedAmount) {
            summary.setStatus(BudgetHealthStatus.OVERRUN.getCode());
            return summary;
        }
        if (consumedAmount * 100 >= approvedAmount * 80) {
            summary.setStatus(BudgetHealthStatus.WARNING.getCode());
            return summary;
        }
        summary.setStatus(BudgetHealthStatus.HEALTHY.getCode());
        return summary;
    }

    private List<BudgetLedgerVO> buildBudgetLedgerVOs(List<BudgetLedger> budgetLedgers) {
        if (budgetLedgers == null || budgetLedgers.isEmpty()) {
            return List.of();
        }
        List<BudgetLedgerVO> vos = budgetLedgerConvert.toVOs(budgetLedgers);
        Map<String, String> decisionTitleMap = loadDecisionTitleMap(budgetLedgers);
        for (BudgetLedgerVO vo : vos) {
            if (vo == null) {
                continue;
            }
            vo.setReferenceDisplayName(resolveLedgerReferenceDisplayName(vo, decisionTitleMap));
        }
        return vos;
    }

    private Map<String, String> loadDecisionTitleMap(Collection<BudgetLedger> budgetLedgers) {
        List<String> decisionIds = budgetLedgers == null
                ? List.of()
                : budgetLedgers.stream()
                        .filter(Objects::nonNull)
                        .filter(this::isDecisionReference)
                        .map(BudgetLedger::getReferenceId)
                        .filter(Objects::nonNull)
                        .filter(id -> !id.isBlank())
                        .distinct()
                        .toList();
        if (decisionIds.isEmpty()) {
            return Map.of();
        }
        return decisionItemService.listByIds(decisionIds).stream()
                .collect(Collectors.toMap(
                        DecisionItem::getId,
                        decision -> firstNonBlank(decision.getTitle(), decision.getId()),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private boolean isDecisionReference(BudgetLedger budgetLedger) {
        return budgetLedger != null && isDecisionReferenceType(budgetLedger.getReferenceType());
    }

    private boolean isDecisionReferenceType(String referenceType) {
        return Objects.equals(referenceType, "decision_item")
                || Objects.equals(referenceType, "decisionItem")
                || Objects.equals(referenceType, "decision");
    }

    private String resolveLedgerReferenceDisplayName(BudgetLedgerVO vo, Map<String, String> decisionTitleMap) {
        if (vo == null || vo.getReferenceId() == null || vo.getReferenceId().isBlank()) {
            return null;
        }
        if (isDecisionReferenceType(vo.getReferenceType())) {
            return decisionTitleMap.getOrDefault(vo.getReferenceId(), vo.getReferenceId());
        }
        return vo.getReferenceId();
    }

    private int countPendingDecisions(List<DecisionItem> decisionItems) {
        return buildDecisionSummary(decisionItems).getPendingCount();
    }

    private int countBlockerPendingDecisions(List<DecisionItem> decisionItems) {
        return buildDecisionSummary(decisionItems).getBlockerCount();
    }

    private ProjectRequirementSummaryVO buildRequirementSummary(
            RequirementIntake intake,
            List<ClarificationItem> clarificationItems) {
        List<ClarificationItem> items = clarificationItems == null ? List.of() : clarificationItems;
        List<String> missingFields = new ArrayList<>();
        int completedFieldCount = 0;

        completedFieldCount += accumulateCoreField(intake == null ? null : intake.getBusinessGoal(), "业务目标", missingFields);
        completedFieldCount += accumulateCoreField(intake == null ? null : intake.getFeatureSummary(), "功能摘要", missingFields);
        completedFieldCount += accumulateCoreField(intake == null ? null : intake.getReferenceProducts(), "参考产品/链接", missingFields);
        completedFieldCount += accumulateCoreField(intake == null ? null : intake.getTimelineExpectation(), "时间预期", missingFields);
        completedFieldCount += accumulateCoreField(intake == null ? null : intake.getBudgetRange(), "预算范围", missingFields);
        completedFieldCount += accumulateCoreField(intake == null ? null : intake.getTechnicalConstraints(), "技术约束", missingFields);

        int totalCoreFieldCount = 6;
        int clarificationCount = items.size();
        int openClarificationCount = (int) items.stream()
                .filter(item -> !Objects.equals(item.getStatus(), ClarificationStatus.RESOLVED.getCode()))
                .count();
        int blockerCount = (int) items.stream()
                .filter(item -> !Objects.equals(item.getStatus(), ClarificationStatus.RESOLVED.getCode()))
                .filter(item -> Objects.equals(item.getSeverity(), ClarificationSeverity.BLOCKER.getCode()))
                .count();

        ProjectRequirementSummaryVO summary = new ProjectRequirementSummaryVO();
        summary.setRequirementIntakeId(intake == null ? null : intake.getId());
        summary.setHasRequirementIntake(intake != null);
        summary.setCompletedCoreFieldCount(completedFieldCount);
        summary.setTotalCoreFieldCount(totalCoreFieldCount);
        summary.setCompletenessScore(totalCoreFieldCount == 0 ? 0 : (completedFieldCount * 100) / totalCoreFieldCount);
        summary.setClarificationCount(clarificationCount);
        summary.setOpenClarificationCount(openClarificationCount);
        summary.setBlockerCount(blockerCount);
        summary.setMissingCoreFields(missingFields);
        summary.setLastIntakeUpdatedAt(intake == null ? null : firstNonNull(intake.getUpdateTime(), intake.getCreateTime()));
        return summary;
    }

    private ProjectCollaborationSummaryVO buildCollaborationSummary(
            List<ClarificationItem> clarificationItems,
            List<DecisionItem> decisionItems,
            List<MeetingRecord> meetingRecords) {
        List<ClarificationItem> collaborationClarifications = safe(clarificationItems).stream()
                .filter(this::isCollaborationClarification)
                .toList();
        List<DecisionItem> decisionBudgetReviews = safe(decisionItems).stream()
                .filter(this::isDecisionBudgetCollaborationApplied)
                .toList();
        List<MeetingRecord> productArchitectureBriefs = safe(meetingRecords).stream()
                .filter(this::isProductArchitectureBriefRecord)
                .toList();

        ProjectRequirementCollaborationSummaryVO requirementCollaboration =
                buildRequirementCollaborationSummary(collaborationClarifications);
        ProjectDecisionBudgetCollaborationSummaryVO decisionBudgetCollaboration =
                buildDecisionBudgetCollaborationSummary(decisionBudgetReviews);
        ProjectProductArchitectureCollaborationSummaryVO productArchitectureCollaboration =
                buildProductArchitectureCollaborationSummary(productArchitectureBriefs);

        List<String> governanceHighlights = buildCollaborationHighlights(
                requirementCollaboration,
                decisionBudgetCollaboration,
                productArchitectureCollaboration);

        ProjectCollaborationSummaryVO summary = new ProjectCollaborationSummaryVO();
        summary.setAppliedCollaborationCount(
                collaborationClarifications.size() + decisionBudgetReviews.size() + productArchitectureBriefs.size());
        summary.setLatestCollaborationAt(java.util.stream.Stream.of(
                        productArchitectureCollaboration.getLatestSavedAt(),
                        decisionBudgetCollaboration.getLatestAppliedAt(),
                        requirementCollaboration.getLatestAppliedAt())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        summary.setGovernanceHighlights(governanceHighlights);
        summary.setRequirementClarification(requirementCollaboration);
        summary.setDecisionBudget(decisionBudgetCollaboration);
        summary.setProductArchitecture(productArchitectureCollaboration);
        return summary;
    }

    private ProjectRequirementCollaborationSummaryVO buildRequirementCollaborationSummary(
            List<ClarificationItem> collaborationClarifications) {
        List<ClarificationItem> items = safe(collaborationClarifications);
        ProjectRequirementCollaborationSummaryVO summary = new ProjectRequirementCollaborationSummaryVO();
        summary.setAppliedClarificationCount(items.size());
        summary.setOpenClarificationCount((int) items.stream()
                .filter(item -> !Objects.equals(item.getStatus(), ClarificationStatus.RESOLVED.getCode()))
                .count());
        summary.setBlockerClarificationCount((int) items.stream()
                .filter(item -> !Objects.equals(item.getStatus(), ClarificationStatus.RESOLVED.getCode()))
                .filter(item -> Objects.equals(item.getSeverity(), ClarificationSeverity.BLOCKER.getCode()))
                .count());
        summary.setDecisionEscalationSuggestionCount((int) items.stream()
                .filter(this::isDecisionEscalationSuggested)
                .count());
        summary.setFollowUpModules(items.stream()
                .map(this::extractClarificationFollowUpModule)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        summary.setLatestAppliedAt(items.stream()
                .map(item -> firstNonNull(item.getUpdateTime(), item.getCreateTime()))
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        return summary;
    }

    private ProjectDecisionBudgetCollaborationSummaryVO buildDecisionBudgetCollaborationSummary(
            List<DecisionItem> decisionBudgetReviews) {
        List<DecisionItem> items = safe(decisionBudgetReviews);
        DecisionItem latestDecision = items.stream()
                .max(Comparator.comparing(
                        item -> firstNonNull(item.getUpdateTime(), item.getCreateTime(), item.getDueAt()),
                        Comparator.nullsLast(LocalDateTime::compareTo)))
                .orElse(null);

        ProjectDecisionBudgetCollaborationSummaryVO summary =
                new ProjectDecisionBudgetCollaborationSummaryVO();
        summary.setAppliedDecisionCount(items.size());
        summary.setOpenDecisionCount((int) items.stream()
                .filter(this::isPendingDecision)
                .count());
        summary.setBlockerDecisionCount((int) items.stream()
                .filter(this::isPendingDecision)
                .filter(item -> Objects.equals(item.getBlockerFlag(), 1))
                .count());
        summary.setBudgetConfirmationSuggestedCount((int) items.stream()
                .filter(this::isBudgetConfirmationSuggested)
                .count());
        summary.setLatestDecisionTitle(latestDecision == null
                ? null
                : firstNonBlank(latestDecision.getTitle(), latestDecision.getId()));
        summary.setLatestRecommendedOption(latestDecision == null
                ? null
                : latestDecision.getRecommendedOption());
        summary.setLatestAppliedAt(items.stream()
                .map(item -> firstNonNull(item.getUpdateTime(), item.getCreateTime(), item.getDueAt()))
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        return summary;
    }

    private ProjectProductArchitectureCollaborationSummaryVO buildProductArchitectureCollaborationSummary(
            List<MeetingRecord> productArchitectureBriefs) {
        List<MeetingRecord> items = safe(productArchitectureBriefs);
        MeetingRecord latestRecord = items.stream()
                .max(Comparator.comparing(
                        item -> firstNonNull(item.getGeneratedAt(), item.getUpdateTime(), item.getCreateTime()),
                        Comparator.nullsLast(LocalDateTime::compareTo)))
                .orElse(null);
        var latestBrief = meetingRecordConvert.toVO(latestRecord);

        ProjectProductArchitectureCollaborationSummaryVO summary =
                new ProjectProductArchitectureCollaborationSummaryVO();
        summary.setBriefCount(items.size());
        summary.setLatestBriefTitle(latestBrief == null ? null : latestBrief.getMeetingTitle());
        summary.setLatestBriefSummary(latestBrief == null ? null : latestBrief.getSummary());
        summary.setLatestOpenQuestionCount(latestBrief == null ? 0 : safe(latestBrief.getOpenQuestions()).size());
        summary.setLatestActionItemCount(latestBrief == null ? 0 : safe(latestBrief.getActionItems()).size());
        summary.setLatestDecisionCandidateCount(latestBrief == null ? 0 : safe(latestBrief.getDecisionCandidates()).size());
        summary.setLatestSavedAt(items.stream()
                .map(item -> firstNonNull(item.getGeneratedAt(), item.getUpdateTime(), item.getCreateTime()))
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        return summary;
    }

    private List<String> buildCollaborationHighlights(
            ProjectRequirementCollaborationSummaryVO requirementCollaboration,
            ProjectDecisionBudgetCollaborationSummaryVO decisionBudgetCollaboration,
            ProjectProductArchitectureCollaborationSummaryVO productArchitectureCollaboration) {
        List<String> highlights = new ArrayList<>();
        if (zeroIfNull(requirementCollaboration.getDecisionEscalationSuggestionCount()) > 0) {
            highlights.add("有 "
                    + zeroIfNull(requirementCollaboration.getDecisionEscalationSuggestionCount())
                    + " 条协作澄清建议人工升级到决策中心");
        }
        if (zeroIfNull(requirementCollaboration.getBlockerClarificationCount()) > 0) {
            highlights.add("仍有 "
                    + zeroIfNull(requirementCollaboration.getBlockerClarificationCount())
                    + " 条阻塞级协作澄清待处理");
        }
        if (zeroIfNull(decisionBudgetCollaboration.getBudgetConfirmationSuggestedCount()) > 0) {
            highlights.add("有 "
                    + zeroIfNull(decisionBudgetCollaboration.getBudgetConfirmationSuggestedCount())
                    + " 条协作评审建议补充预算确认");
        }
        if (zeroIfNull(productArchitectureCollaboration.getLatestOpenQuestionCount()) > 0) {
            highlights.add("最近产品与架构协作简报仍保留 "
                    + zeroIfNull(productArchitectureCollaboration.getLatestOpenQuestionCount())
                    + " 个开放问题");
        }
        if ((requirementCollaboration.getFollowUpModules() != null
                && !requirementCollaboration.getFollowUpModules().isEmpty())) {
            highlights.add("协作澄清建议优先联动："
                    + String.join(" / ", requirementCollaboration.getFollowUpModules()));
        }
        return highlights;
    }

    private int accumulateCoreField(String value, String label, List<String> missingFields) {
        if (value != null && !value.isBlank()) {
            return 1;
        }
        missingFields.add(label);
        return 0;
    }

    private List<ProjectActivityVO> buildActivities(
            Project project,
            RequirementIntake requirementIntake,
            List<ClarificationItem> clarificationItems,
            List<ProjectStage> stages,
            List<DecisionItem> decisionItems,
            List<ApprovalRecord> approvalRecords,
            List<BudgetLedger> budgetLedgers,
            List<MeetingRecord> meetingRecords,
            List<ToolIntegrationAudit> toolIntegrationAudits) {
        List<ProjectActivityVO> activities = new ArrayList<>();

        activities.add(activity(
                "project",
                "项目已更新",
                firstNonBlank(project.getProjectName(), project.getProjectCode(), "项目") + " 当前状态为 "
                        + firstNonBlank(project.getStatus(), "-"),
                project.getStatus(),
                firstNonNull(project.getUpdateTime(), project.getCreateTime())));

        if (requirementIntake != null) {
            activities.add(activity(
                    "intake",
                    "需求接收已更新",
                    firstNonBlank(requirementIntake.getFeatureSummary(), requirementIntake.getBusinessGoal(), "需求接收内容已更新"),
                    "updated",
                    firstNonNull(requirementIntake.getUpdateTime(), requirementIntake.getCreateTime())));
        }

        for (ClarificationItem clarificationItem : limitList(clarificationItems, DETAIL_ITEM_LIMIT)) {
            activities.add(activity(
                    "clarification",
                    isCollaborationClarification(clarificationItem) ? "多角色澄清评审已应用" : "澄清项更新",
                    firstNonBlank(clarificationItem.getTitle(), clarificationItem.getQuestion(), "澄清项"),
                    clarificationItem.getStatus(),
                    firstNonNull(clarificationItem.getUpdateTime(), clarificationItem.getCreateTime())));
        }

        for (ProjectStage stage : limitList(stages, DETAIL_ITEM_LIMIT)) {
            LocalDateTime occurredAt = firstNonNull(stage.getEndedAt(), stage.getStartedAt(), stage.getUpdateTime(), stage.getCreateTime());
            String title = stage.getEndedAt() != null ? "阶段已完成" : stage.getStartedAt() != null ? "阶段进行中" : "阶段已更新";
            String description = firstNonBlank(stage.getStageName(), lifecycleStageLabel(stage.getStageCode()), stage.getStageCode());
            activities.add(activity("stage", title, description, stage.getGateStatus(), occurredAt));
        }

        for (DecisionItem decisionItem : limitList(decisionItems, DETAIL_ITEM_LIMIT)) {
            activities.add(activity(
                    "decision",
                    isDecisionBudgetCollaborationApplied(decisionItem) ? "决策与预算协作评审已应用" : "决策事项更新",
                    firstNonBlank(
                            isDecisionBudgetCollaborationApplied(decisionItem)
                                    ? decisionItem.getRecommendedOption()
                                    : null,
                            decisionItem.getTitle(),
                            decisionItem.getItemType(),
                            "决策事项"),
                    decisionItem.getStatus(),
                    firstNonNull(decisionItem.getUpdateTime(), decisionItem.getCreateTime(), decisionItem.getDueAt())));
        }

        for (ApprovalRecord approvalRecord : limitList(approvalRecords, DETAIL_ITEM_LIMIT)) {
            activities.add(activity(
                    "approval",
                    "审批记录更新",
                    firstNonBlank(approvalRecord.getTitle(), approvalRecord.getApprovalType(), "审批"),
                    approvalRecord.getApprovalStatus(),
                    firstNonNull(approvalRecord.getDecidedAt(), approvalRecord.getSubmittedAt(), approvalRecord.getCreateTime())));
        }

        for (BudgetLedger budgetLedger : limitList(budgetLedgers, DETAIL_ITEM_LIMIT)) {
            activities.add(activity(
                    "budget",
                    "预算流水更新",
                    firstNonBlank(budgetLedger.getDescription(), budgetLedger.getEntryType(), "预算流水"),
                    budgetLedger.getEntryType(),
                    firstNonNull(budgetLedger.getOccurredAt(), budgetLedger.getCreateTime())));
        }

        for (MeetingRecord meetingRecord : limitList(meetingRecords, DETAIL_ITEM_LIMIT)) {
            activities.add(activity(
                    "meeting",
                    isProductArchitectureBriefRecord(meetingRecord) ? "产品与架构协作简报已保存" : "会议总结已生成",
                    firstNonBlank(
                            isProductArchitectureBriefRecord(meetingRecord) ? meetingRecord.getRemark() : null,
                            meetingRecord.getMeetingTitle(),
                            meetingRecord.getSummary(),
                            "会议记录"),
                    firstNonBlank(meetingRecord.getSourceType(), "generated"),
                    firstNonNull(meetingRecord.getGeneratedAt(), meetingRecord.getUpdateTime(), meetingRecord.getCreateTime())));
        }

        for (ToolIntegrationAudit audit : limitList(toolIntegrationAudits, DETAIL_ITEM_LIMIT)) {
            ProjectActivityVO activity = activity(
                    "integration",
                    "外部工具动作",
                    firstNonBlank(audit.getActionType(), audit.getToolType(), "tool_integration"),
                    audit.getAuditStatus(),
                    firstNonNull(audit.getCreateTime(), project.getUpdateTime(), project.getCreateTime()));
            activity.setExternalUrl(audit.getExternalObjectUrl());
            activities.add(activity);
        }

        return activities.stream()
                .filter(item -> item.getOccurredAt() != null)
                .sorted(Comparator.comparing(ProjectActivityVO::getOccurredAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .limit(ACTIVITY_LIMIT)
                .toList();
    }

    private ProjectActivityVO activity(String type, String title, String description, String status, LocalDateTime occurredAt) {
        ProjectActivityVO activity = new ProjectActivityVO();
        activity.setActivityType(type);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setStatus(status);
        activity.setOccurredAt(occurredAt);
        return activity;
    }

    private String lifecycleStageLabel(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (ProjectLifecycleStage stage : ProjectLifecycleStage.values()) {
            if (Objects.equals(stage.getCode(), code)) {
                return stage.getLabel();
            }
        }
        return code;
    }

    private boolean isCollaborationClarification(ClarificationItem clarificationItem) {
        return clarificationItem != null
                && clarificationItem.getRemark() != null
                && clarificationItem.getRemark().contains("来源：多角色澄清评审");
    }

    private boolean isDecisionBudgetCollaborationApplied(DecisionItem decisionItem) {
        return decisionItem != null
                && decisionItem.getRemark() != null
                && decisionItem.getRemark().contains("已应用多角色协作评审建议");
    }

    private boolean isDecisionEscalationSuggested(ClarificationItem clarificationItem) {
        return clarificationItem != null
                && clarificationItem.getRemark() != null
                && clarificationItem.getRemark().contains("建议人工判断后提升至决策中心");
    }

    private String extractClarificationFollowUpModule(ClarificationItem clarificationItem) {
        if (clarificationItem == null || clarificationItem.getRemark() == null) {
            return null;
        }
        return extractRemarkValue(clarificationItem.getRemark(), "建议跟进模块：");
    }

    private boolean isBudgetConfirmationSuggested(DecisionItem decisionItem) {
        return decisionItem != null
                && decisionItem.getRemark() != null
                && decisionItem.getRemark().contains("建议补充预算确认");
    }

    private boolean isPendingDecision(DecisionItem decisionItem) {
        return decisionItem != null
                && (Objects.equals(decisionItem.getStatus(), DecisionItemStatus.OPEN.getCode())
                || Objects.equals(decisionItem.getStatus(), DecisionItemStatus.PENDING_APPROVAL.getCode()));
    }

    private boolean isProductArchitectureBriefRecord(MeetingRecord meetingRecord) {
        return meetingRecord != null
                && Objects.equals(meetingRecord.getSourceType(), "product_architecture_brief");
    }

    private String extractRemarkValue(String remark, String prefix) {
        if (remark == null || remark.isBlank() || prefix == null || prefix.isBlank()) {
            return null;
        }
        for (String segment : remark.split("\\s*\\|\\s*")) {
            if (segment != null && segment.startsWith(prefix)) {
                String value = segment.substring(prefix.length()).trim();
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return null;
    }

    private <T> List<T> limitList(List<T> items, int limit) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream().limit(limit).toList();
    }

    private <T> List<T> safe(List<T> items) {
        return items == null ? List.of() : items;
    }

    private List<java.io.Serializable> normalizeIds(Collection<?> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> (java.io.Serializable) item)
                .toList();
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

    private long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean isPendingApproval(ApprovalRecord approvalRecord) {
        return approvalRecord != null
                && (Objects.equals(approvalRecord.getApprovalStatus(), ApprovalStatus.DRAFT.getCode())
                || Objects.equals(approvalRecord.getApprovalStatus(), ApprovalStatus.SUBMITTED.getCode())
                || Objects.equals(approvalRecord.getApprovalStatus(), ApprovalStatus.REQUEST_CHANGES.getCode())
                || Objects.equals(approvalRecord.getApprovalStatus(), ApprovalStatus.DEFERRED.getCode()));
    }

    private record ResolvedStage(String stageCode, String stageName, String stageStatus, String gateStatus) {
    }

    private record RoleResolution(
            List<ProjectAgentRoleSummaryVO> linkedRoles,
            List<ProjectAgentRoleSummaryVO> currentStageRecommendedRoles,
            List<String> missingCriticalRoles) {
    }
}
