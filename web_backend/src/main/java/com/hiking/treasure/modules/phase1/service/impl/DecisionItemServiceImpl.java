package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.modules.phase1.domain.dto.action.DecisionActionRequestDTO;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.DecisionActionLog;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationCategory;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationSeverity;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionActionType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionItemStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionItemType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionPriority;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.DecisionSourceType;
import com.hiking.treasure.modules.phase1.mapper.DecisionItemMapper;
import com.hiking.treasure.modules.phase1.service.ClarificationItemService;
import com.hiking.treasure.modules.phase1.service.DecisionActionLogService;
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
public class DecisionItemServiceImpl extends ServiceImpl<DecisionItemMapper, DecisionItem> implements DecisionItemService {

    private final ClarificationItemService clarificationItemService;
    private final DecisionActionLogService decisionActionLogService;
    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Override
    public boolean save(DecisionItem entity) {
        boolean saved = super.save(entity);
        if (saved) {
            projectGovernanceLinkageService.recomputeProject(entity == null ? null : entity.getProjectId());
        }
        return saved;
    }

    @Override
    public boolean updateById(DecisionItem entity) {
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
                .map(DecisionItem::getProjectId)
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
    public DecisionItem takeAction(String decisionItemId, DecisionActionRequestDTO dto) {
        DecisionItem decisionItem = getById(decisionItemId);
        if (decisionItem == null) {
            throw new BusinessException(404, "决策事项不存在");
        }
        if (dto == null || dto.getActionType() == null || dto.getActionType().isBlank()) {
            throw new BusinessException(400, "动作类型不能为空");
        }

        String previousStatus = decisionItem.getStatus();
        String nextStatus = resolveNextStatus(dto.getActionType());
        decisionItem.setStatus(nextStatus);
        updateById(decisionItem);

        logAction(decisionItem.getId(), dto.getActionType(), dto.getComment(), previousStatus, nextStatus);
        return getById(decisionItemId);
    }

    @Override
    public List<DecisionActionLog> listActionLogs(String decisionItemId) {
        return decisionActionLogService.list(new LambdaQueryWrapper<DecisionActionLog>()
                .eq(DecisionActionLog::getDecisionItemId, decisionItemId)
                .orderByDesc(DecisionActionLog::getOperatedAt)
                .orderByDesc(DecisionActionLog::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DecisionItem promoteFromClarification(String clarificationItemId) {
        ClarificationItem clarificationItem = clarificationItemService.getById(clarificationItemId);
        if (clarificationItem == null) {
            throw new BusinessException(404, "澄清项不存在");
        }
        if (clarificationItem.getPromotedDecisionItemId() != null
                && !clarificationItem.getPromotedDecisionItemId().isBlank()) {
            DecisionItem existing = getById(clarificationItem.getPromotedDecisionItemId());
            if (existing != null) {
                return existing;
            }
        }

        DecisionItem decisionItem = new DecisionItem();
        decisionItem.setProjectId(clarificationItem.getProjectId());
        decisionItem.setTitle(firstNonBlank(
                clarificationItem.getTitle(),
                "待确认事项"));
        decisionItem.setItemType(resolveDecisionType(clarificationItem));
        decisionItem.setSourceType(DecisionSourceType.CLARIFICATION.getCode());
        decisionItem.setSourceId(clarificationItem.getId());
        decisionItem.setDescription(firstNonBlank(
                clarificationItem.getQuestion(),
                clarificationItem.getTitle(),
                "该澄清事项需要升级为正式决策"));
        decisionItem.setImpactSummary(buildImpactSummary(clarificationItem));
        decisionItem.setSuggestedOptions(clarificationItem.getSuggestedOptions());
        decisionItem.setRecommendedOption(buildRecommendedOption(clarificationItem));
        decisionItem.setBudgetImpactSummary(buildBudgetImpactSummary(clarificationItem));
        decisionItem.setProjectImpactSummary(buildProjectImpactSummary(clarificationItem));
        decisionItem.setBlockerFlag(isBlockerClarification(clarificationItem) ? 1 : 0);
        decisionItem.setPriority(resolvePriority(clarificationItem));
        decisionItem.setStatus(DecisionItemStatus.OPEN.getCode());
        decisionItem.setRequestedByUserId(SecurityUtils.getUserId());
        decisionItem.setDueAt(LocalDateTime.now().plusDays(isBlockerClarification(clarificationItem) ? 2 : 5));
        decisionItem.setRemark("由澄清项手动提升为正式决策事项");
        save(decisionItem);

        clarificationItem.setPromotedDecisionItemId(decisionItem.getId());
        clarificationItemService.updateById(clarificationItem);

        logAction(
                decisionItem.getId(),
                DecisionActionType.PROMOTED.getCode(),
                "由澄清项提升为正式决策事项",
                null,
                decisionItem.getStatus());

        return decisionItem;
    }

    private void logAction(
            String decisionItemId,
            String actionType,
            String comment,
            String previousStatus,
            String nextStatus) {
        DecisionActionLog actionLog = new DecisionActionLog();
        actionLog.setDecisionItemId(decisionItemId);
        actionLog.setActionType(actionType);
        actionLog.setActionComment(comment);
        actionLog.setPreviousStatus(previousStatus);
        actionLog.setNextStatus(nextStatus);
        actionLog.setOperatorUserId(SecurityUtils.getUserId());
        actionLog.setOperatedAt(LocalDateTime.now());
        decisionActionLogService.save(actionLog);
    }

    private String resolveNextStatus(String actionType) {
        if (Objects.equals(actionType, DecisionActionType.CONFIRM.getCode())) {
            return DecisionItemStatus.CONFIRMED.getCode();
        }
        if (Objects.equals(actionType, DecisionActionType.REJECT.getCode())) {
            return DecisionItemStatus.REJECTED.getCode();
        }
        if (Objects.equals(actionType, DecisionActionType.DEFER.getCode())) {
            return DecisionItemStatus.DEFERRED.getCode();
        }
        throw new BusinessException(400, "不支持的决策动作");
    }

    private String resolveDecisionType(ClarificationItem clarificationItem) {
        if (clarificationItem == null || clarificationItem.getCategory() == null) {
            return DecisionItemType.CLARIFICATION.getCode();
        }
        if (Objects.equals(clarificationItem.getCategory(), ClarificationCategory.BUDGET.getCode())) {
            return DecisionItemType.BUDGET_CHANGE.getCode();
        }
        if (Objects.equals(clarificationItem.getCategory(), ClarificationCategory.TIMELINE.getCode())) {
            return DecisionItemType.TIMELINE_CONFIRMATION.getCode();
        }
        if (Objects.equals(clarificationItem.getCategory(), ClarificationCategory.FEATURE_SCOPE.getCode())) {
            return DecisionItemType.SCOPE_CHANGE.getCode();
        }
        return DecisionItemType.CLARIFICATION.getCode();
    }

    private String resolvePriority(ClarificationItem clarificationItem) {
        if (clarificationItem == null || clarificationItem.getSeverity() == null) {
            return DecisionPriority.MEDIUM.getCode();
        }
        if (Objects.equals(clarificationItem.getSeverity(), ClarificationSeverity.BLOCKER.getCode())) {
            return DecisionPriority.CRITICAL.getCode();
        }
        if (Objects.equals(clarificationItem.getSeverity(), ClarificationSeverity.HIGH.getCode())) {
            return DecisionPriority.HIGH.getCode();
        }
        if (Objects.equals(clarificationItem.getSeverity(), ClarificationSeverity.LOW.getCode())) {
            return DecisionPriority.LOW.getCode();
        }
        return DecisionPriority.MEDIUM.getCode();
    }

    private String buildImpactSummary(ClarificationItem clarificationItem) {
        return isBlockerClarification(clarificationItem)
                ? "该事项在确认前会阻塞项目关键推进路径。"
                : "该事项未确认前会增加需求、估算或交付方案的不确定性。";
    }

    private String buildRecommendedOption(ClarificationItem clarificationItem) {
        if (clarificationItem == null || clarificationItem.getSuggestedOptions() == null
                || clarificationItem.getSuggestedOptions().isBlank()) {
            return null;
        }
        return "优先采用经业务确认的首选方案";
    }

    private String buildBudgetImpactSummary(ClarificationItem clarificationItem) {
        if (clarificationItem != null
                && Objects.equals(clarificationItem.getCategory(), ClarificationCategory.BUDGET.getCode())) {
            return "预算范围未确认前，当前估算与资源配置存在偏差风险。";
        }
        return "当前暂无自动预算重算，待决策后再进入预算评估。";
    }

    private String buildProjectImpactSummary(ClarificationItem clarificationItem) {
        if (isBlockerClarification(clarificationItem)) {
            return "若继续悬而未决，将直接影响项目阶段推进和范围锁定。";
        }
        return "该事项会影响项目范围、计划或交付预期，需要显式确认。";
    }

    private boolean isBlockerClarification(ClarificationItem clarificationItem) {
        return clarificationItem != null
                && Objects.equals(clarificationItem.getSeverity(), ClarificationSeverity.BLOCKER.getCode());
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

    private String resolveProjectId(String id, String fallbackProjectId) {
        if (fallbackProjectId != null && !fallbackProjectId.isBlank()) {
            return fallbackProjectId;
        }
        if (id == null || id.isBlank()) {
            return null;
        }
        return lambdaQuery()
                .select(DecisionItem::getProjectId)
                .eq(DecisionItem::getId, id)
                .last("LIMIT 1")
                .oneOpt()
                .map(DecisionItem::getProjectId)
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
