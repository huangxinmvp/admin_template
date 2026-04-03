package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.modules.phase1.domain.dto.action.ApprovalActionRequestDTO;
import com.hiking.treasure.modules.phase1.entity.ApprovalActionLog;
import com.hiking.treasure.modules.phase1.entity.ApprovalRecord;
import com.hiking.treasure.modules.phase1.entity.BudgetPlan;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalActionType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalSourceObjectType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ApprovalType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetPlanStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionItemType;
import com.hiking.treasure.modules.phase1.mapper.ApprovalRecordMapper;
import com.hiking.treasure.modules.phase1.service.ApprovalActionLogService;
import com.hiking.treasure.modules.phase1.service.ApprovalRecordService;
import com.hiking.treasure.modules.phase1.service.BudgetPlanService;
import com.hiking.treasure.modules.phase1.service.DecisionItemService;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApprovalRecordServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord>
        implements ApprovalRecordService {

    private final ApprovalActionLogService approvalActionLogService;
    private final DecisionItemService decisionItemService;
    private final BudgetPlanService budgetPlanService;
    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Override
    public boolean save(ApprovalRecord entity) {
        boolean saved = super.save(entity);
        if (saved) {
            projectGovernanceLinkageService.recomputeProject(entity == null ? null : entity.getProjectId());
        }
        return saved;
    }

    @Override
    public boolean updateById(ApprovalRecord entity) {
        String projectId = resolveProjectId(entity == null ? null : entity.getId(), entity == null ? null : entity.getProjectId());
        boolean updated = super.updateById(entity);
        if (updated) {
            projectGovernanceLinkageService.recomputeProject(projectId);
        }
        return updated;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        String projectId = resolveProjectId(Objects.toString(id, null), null);
        boolean removed = super.removeById(id);
        if (removed) {
            projectGovernanceLinkageService.recomputeProject(projectId);
        }
        return removed;
    }

    @Override
    public boolean removeByIds(java.util.Collection<?> list) {
        List<java.io.Serializable> ids = normalizeIds(list);
        List<String> projectIds = listByIds(ids).stream()
                .map(ApprovalRecord::getProjectId)
                .filter(Objects::nonNull)
                .toList();
        boolean removed = super.removeByIds(ids);
        if (removed) {
            projectGovernanceLinkageService.recomputeProjects(projectIds);
        }
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalRecord takeAction(String approvalRecordId, ApprovalActionRequestDTO dto) {
        ApprovalRecord approvalRecord = getById(approvalRecordId);
        if (approvalRecord == null) {
            throw new BusinessException(404, "审批记录不存在");
        }
        if (dto == null || dto.getActionType() == null || dto.getActionType().isBlank()) {
            throw new BusinessException(400, "动作类型不能为空");
        }

        String previousStatus = approvalRecord.getApprovalStatus();
        String nextStatus = resolveNextStatus(dto.getActionType());
        LocalDateTime operatedAt = LocalDateTime.now();

        approvalRecord.setApprovalStatus(nextStatus);
        approvalRecord.setDecisionNote(firstNonBlank(dto.getComment(), approvalRecord.getDecisionNote()));
        approvalRecord.setOperatorUserId(SecurityUtils.getUserId());
        approvalRecord.setDecidedAt(operatedAt);
        updateById(approvalRecord);

        logAction(approvalRecord.getId(), dto.getActionType(), dto.getComment(), previousStatus, nextStatus, operatedAt);
        return getById(approvalRecordId);
    }

    @Override
    public List<ApprovalActionLog> listActionLogs(String approvalRecordId) {
        return approvalActionLogService.list(new LambdaQueryWrapper<ApprovalActionLog>()
                .eq(ApprovalActionLog::getApprovalRecordId, approvalRecordId)
                .orderByDesc(ApprovalActionLog::getOperatedAt)
                .orderByDesc(ApprovalActionLog::getCreateTime));
    }

    @Override
    public List<ApprovalRecord> listBySource(String sourceObjectType, String sourceObjectId) {
        return list(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getSourceObjectType, sourceObjectType)
                .eq(ApprovalRecord::getSourceObjectId, sourceObjectId)
                .orderByDesc(ApprovalRecord::getSubmittedAt)
                .orderByDesc(ApprovalRecord::getUpdateTime)
                .orderByDesc(ApprovalRecord::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalRecord createOrOpenForDecision(String decisionItemId) {
        DecisionItem decisionItem = decisionItemService.getById(decisionItemId);
        if (decisionItem == null) {
            throw new BusinessException(404, "决策事项不存在");
        }

        ApprovalRecord existing = findLatestLinkedApproval(
                ApprovalSourceObjectType.DECISION_ITEM.getCode(),
                decisionItemId);
        if (existing != null) {
            return existing;
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        String decisionTitle = firstNonBlank(decisionItem.getTitle(), "待确认事项");
        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setProjectId(decisionItem.getProjectId());
        approvalRecord.setTitle("决策审批：" + decisionTitle);
        approvalRecord.setDecisionItemId(decisionItem.getId());
        approvalRecord.setApprovalType(resolveDecisionApprovalType(decisionItem));
        approvalRecord.setSourceObjectType(ApprovalSourceObjectType.DECISION_ITEM.getCode());
        approvalRecord.setSourceObjectId(decisionItem.getId());
        approvalRecord.setRequesterUserId(firstNonBlank(decisionItem.getRequestedByUserId(), SecurityUtils.getUserId()));
        approvalRecord.setApproverUserId(decisionItem.getAssigneeUserId());
        approvalRecord.setDescription(firstNonBlank(decisionItem.getDescription(), decisionItem.getImpactSummary()));
        approvalRecord.setRiskSummary(firstNonBlank(decisionItem.getProjectImpactSummary(), decisionItem.getImpactSummary()));
        approvalRecord.setBudgetImpactSummary(decisionItem.getBudgetImpactSummary());
        approvalRecord.setRecommendedAction(firstNonBlank(decisionItem.getRecommendedOption(), "请确认推荐决策方向"));
        approvalRecord.setBlockerFlag(decisionItem.getBlockerFlag() == null ? 0 : decisionItem.getBlockerFlag());
        approvalRecord.setApprovalStatus(ApprovalStatus.SUBMITTED.getCode());
        approvalRecord.setOperatorUserId(SecurityUtils.getUserId());
        approvalRecord.setSubmittedAt(submittedAt);
        approvalRecord.setRemark("由决策事项显式发起审批");
        save(approvalRecord);

        logAction(
                approvalRecord.getId(),
                ApprovalActionType.CREATED.getCode(),
                "由决策事项发起审批",
                null,
                approvalRecord.getApprovalStatus(),
                submittedAt);

        return getById(approvalRecord.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalRecord createOrOpenForBudgetPlan(String budgetPlanId) {
        BudgetPlan budgetPlan = budgetPlanService.getById(budgetPlanId);
        if (budgetPlan == null) {
            throw new BusinessException(404, "预算计划不存在");
        }

        ApprovalRecord existing = findLatestLinkedApproval(
                ApprovalSourceObjectType.BUDGET_PLAN.getCode(),
                budgetPlanId);
        if (existing != null) {
            return existing;
        }

        long proposedAmount = zeroIfNull(budgetPlan.getProposedAmount());
        long approvedAmount = zeroIfNull(budgetPlan.getApprovedAmount());
        long reservedAmount = zeroIfNull(budgetPlan.getReservedAmount());
        long consumedAmount = zeroIfNull(budgetPlan.getConsumedAmount());
        long pendingIncreaseAmount = Math.max(proposedAmount - approvedAmount, 0L);
        LocalDateTime submittedAt = LocalDateTime.now();

        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setProjectId(budgetPlan.getProjectId());
        approvalRecord.setTitle("预算审批：" + firstNonBlank(budgetPlan.getPlanName(), "预算计划"));
        approvalRecord.setApprovalType(pendingIncreaseAmount > 0L
                ? ApprovalType.BUDGET_CHANGE.getCode()
                : ApprovalType.BUDGET.getCode());
        approvalRecord.setSourceObjectType(ApprovalSourceObjectType.BUDGET_PLAN.getCode());
        approvalRecord.setSourceObjectId(budgetPlan.getId());
        approvalRecord.setRequesterUserId(SecurityUtils.getUserId());
        approvalRecord.setDescription(buildBudgetPlanDescription(budgetPlan));
        approvalRecord.setRiskSummary(buildBudgetPlanRiskSummary(budgetPlan, pendingIncreaseAmount));
        approvalRecord.setBudgetImpactSummary(buildBudgetPlanImpactSummary(
                budgetPlan,
                proposedAmount,
                approvedAmount,
                reservedAmount,
                consumedAmount,
                pendingIncreaseAmount));
        approvalRecord.setRecommendedAction(pendingIncreaseAmount > 0L
                ? "请确认是否批准本次预算增补"
                : "请确认当前预算配置与消耗约束");
        approvalRecord.setBlockerFlag(isBlockingBudgetPlan(budgetPlan, pendingIncreaseAmount) ? 1 : 0);
        approvalRecord.setApprovalStatus(ApprovalStatus.SUBMITTED.getCode());
        approvalRecord.setOperatorUserId(SecurityUtils.getUserId());
        approvalRecord.setSubmittedAt(submittedAt);
        approvalRecord.setRemark("由预算计划显式发起审批");
        save(approvalRecord);

        logAction(
                approvalRecord.getId(),
                ApprovalActionType.CREATED.getCode(),
                "由预算计划发起审批",
                null,
                approvalRecord.getApprovalStatus(),
                submittedAt);

        return getById(approvalRecord.getId());
    }

    private ApprovalRecord findLatestLinkedApproval(String sourceObjectType, String sourceObjectId) {
        return getOne(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getSourceObjectType, sourceObjectType)
                .eq(ApprovalRecord::getSourceObjectId, sourceObjectId)
                .ne(ApprovalRecord::getApprovalStatus, ApprovalStatus.CANCELLED.getCode())
                .orderByDesc(ApprovalRecord::getSubmittedAt)
                .orderByDesc(ApprovalRecord::getUpdateTime)
                .orderByDesc(ApprovalRecord::getCreateTime)
                .last("LIMIT 1"), false);
    }

    private void logAction(
            String approvalRecordId,
            String actionType,
            String comment,
            String previousStatus,
            String nextStatus,
            LocalDateTime operatedAt) {
        ApprovalActionLog actionLog = new ApprovalActionLog();
        actionLog.setApprovalRecordId(approvalRecordId);
        actionLog.setActionType(actionType);
        actionLog.setActionComment(comment);
        actionLog.setPreviousStatus(previousStatus);
        actionLog.setNextStatus(nextStatus);
        actionLog.setOperatorUserId(SecurityUtils.getUserId());
        actionLog.setOperatedAt(operatedAt);
        approvalActionLogService.save(actionLog);
    }

    private String resolveNextStatus(String actionType) {
        if (Objects.equals(actionType, ApprovalActionType.APPROVE.getCode())) {
            return ApprovalStatus.APPROVED.getCode();
        }
        if (Objects.equals(actionType, ApprovalActionType.REJECT.getCode())) {
            return ApprovalStatus.REJECTED.getCode();
        }
        if (Objects.equals(actionType, ApprovalActionType.REQUEST_CHANGES.getCode())) {
            return ApprovalStatus.REQUEST_CHANGES.getCode();
        }
        if (Objects.equals(actionType, ApprovalActionType.DEFER.getCode())) {
            return ApprovalStatus.DEFERRED.getCode();
        }
        throw new BusinessException(400, "不支持的审批动作");
    }

    private String resolveDecisionApprovalType(DecisionItem decisionItem) {
        if (decisionItem == null || decisionItem.getItemType() == null) {
            return ApprovalType.DECISION.getCode();
        }
        if (Objects.equals(decisionItem.getItemType(), DecisionItemType.BUDGET_CHANGE.getCode())) {
            return ApprovalType.BUDGET_CHANGE.getCode();
        }
        return ApprovalType.DECISION.getCode();
    }

    private String buildBudgetPlanDescription(BudgetPlan budgetPlan) {
        return firstNonBlank(
                budgetPlan.getRemark(),
                "预算计划已发生需要确认的预算配置或增补变动");
    }

    private String buildBudgetPlanRiskSummary(BudgetPlan budgetPlan, long pendingIncreaseAmount) {
        long approvedAmount = zeroIfNull(budgetPlan.getApprovedAmount());
        long consumedAmount = zeroIfNull(budgetPlan.getConsumedAmount());
        if (pendingIncreaseAmount > 0L) {
            return "当前预算提议高于已批准预算，若不确认将影响后续资源承诺。";
        }
        if (approvedAmount > 0L && consumedAmount >= approvedAmount) {
            return "当前预算已触达或超过批准上限，存在超支风险。";
        }
        if (Objects.equals(budgetPlan.getStatus(), BudgetPlanStatus.DRAFT.getCode())
                || Objects.equals(budgetPlan.getStatus(), BudgetPlanStatus.PENDING_APPROVAL.getCode())) {
            return "预算计划仍处于待确认状态，后续执行边界尚未正式锁定。";
        }
        return "该预算计划需要进入正式审批层以形成可追踪确认记录。";
    }

    private String buildBudgetPlanImpactSummary(
            BudgetPlan budgetPlan,
            long proposedAmount,
            long approvedAmount,
            long reservedAmount,
            long consumedAmount,
            long pendingIncreaseAmount) {
        String currencyCode = firstNonBlank(budgetPlan.getCurrencyCode(), "TOKEN");
        return "提议 "
                + proposedAmount
                + " "
                + currencyCode
                + "，已批准 "
                + approvedAmount
                + " "
                + currencyCode
                + "，已锁定 "
                + reservedAmount
                + " "
                + currencyCode
                + "，已消耗 "
                + consumedAmount
                + " "
                + currencyCode
                + "，待增补 "
                + pendingIncreaseAmount
                + " "
                + currencyCode;
    }

    private boolean isBlockingBudgetPlan(BudgetPlan budgetPlan, long pendingIncreaseAmount) {
        long approvedAmount = zeroIfNull(budgetPlan.getApprovedAmount());
        long consumedAmount = zeroIfNull(budgetPlan.getConsumedAmount());
        return pendingIncreaseAmount > 0L
                || (approvedAmount > 0L && consumedAmount >= approvedAmount);
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

    private String resolveProjectId(String id, String fallbackProjectId) {
        if (fallbackProjectId != null && !fallbackProjectId.isBlank()) {
            return fallbackProjectId;
        }
        if (id == null || id.isBlank()) {
            return null;
        }
        return lambdaQuery()
                .select(ApprovalRecord::getProjectId)
                .eq(ApprovalRecord::getId, id)
                .last("LIMIT 1")
                .oneOpt()
                .map(ApprovalRecord::getProjectId)
                .orElse(null);
    }

    private List<java.io.Serializable> normalizeIds(java.util.Collection<?> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> (java.io.Serializable) item)
                .toList();
    }
}
