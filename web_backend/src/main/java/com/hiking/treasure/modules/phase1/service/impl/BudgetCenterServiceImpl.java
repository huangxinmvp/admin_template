package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.domain.convert.BudgetLedgerConvert;
import com.hiking.treasure.modules.phase1.domain.convert.BudgetPlanConvert;
import com.hiking.treasure.modules.phase1.domain.dto.query.BudgetCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetCenterListVO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetLedgerVO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetPlanVO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetRoleAllocationVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectBudgetSummaryVO;
import com.hiking.treasure.modules.phase1.entity.BudgetLedger;
import com.hiking.treasure.modules.phase1.entity.BudgetPlan;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetHealthStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetPlanStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetRoleCode;
import com.hiking.treasure.modules.phase1.service.BudgetCenterService;
import com.hiking.treasure.modules.phase1.service.BudgetLedgerService;
import com.hiking.treasure.modules.phase1.service.BudgetPlanService;
import com.hiking.treasure.modules.phase1.service.DecisionItemService;
import com.hiking.treasure.modules.phase1.service.ProjectService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetCenterServiceImpl implements BudgetCenterService {

    private static final int DETAIL_LEDGER_LIMIT = 20;

    private final ProjectService projectService;
    private final BudgetPlanService budgetPlanService;
    private final BudgetLedgerService budgetLedgerService;
    private final DecisionItemService decisionItemService;
    private final BudgetPlanConvert budgetPlanConvert;
    private final BudgetLedgerConvert budgetLedgerConvert;
    private final ObjectMapper objectMapper;

    @Override
    public Page<BudgetCenterListVO> pageBudgetCenter(BudgetCenterQueryDTO dto, long pageNo, long pageSize) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (dto.getProjectId() != null && !dto.getProjectId().isBlank()) {
                wrapper.eq(Project::getId, dto.getProjectId());
            }
            if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
                wrapper.and(q -> q.like(Project::getProjectName, dto.getKeyword())
                        .or()
                        .like(Project::getProjectCode, dto.getKeyword()));
            }
            if (dto.getProjectType() != null && !dto.getProjectType().isBlank()) {
                wrapper.eq(Project::getProjectType, dto.getProjectType());
            }
        }
        wrapper.orderByDesc(Project::getUpdateTime).orderByDesc(Project::getCreateTime);

        List<Project> projects = projectService.list(wrapper);
        if (projects.isEmpty()) {
            return new Page<>(pageNo, pageSize, 0L);
        }

        List<String> projectIds = projects.stream().map(Project::getId).filter(Objects::nonNull).toList();
        Map<String, BudgetPlan> latestPlansByProject = loadLatestBudgetPlans(projectIds);

        List<BudgetCenterListVO> items = projects.stream()
                .map(project -> buildListItem(project, latestPlansByProject.get(project.getId())))
                .filter(item -> matchesBudgetStatus(item, dto == null ? null : dto.getBudgetStatus()))
                .toList();

        long total = items.size();
        int fromIndex = (int) Math.max((pageNo - 1) * pageSize, 0);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        List<BudgetCenterListVO> pageRecords = fromIndex >= toIndex ? List.of() : items.subList(fromIndex, toIndex);

        Page<BudgetCenterListVO> page = new Page<>(pageNo, pageSize, total);
        page.setRecords(pageRecords);
        return page;
    }

    @Override
    public BudgetCenterDetailVO getProjectBudgetDetail(String projectId) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }

        List<BudgetPlan> budgetPlans = budgetPlanService.list(new LambdaQueryWrapper<BudgetPlan>()
                .eq(BudgetPlan::getProjectId, projectId)
                .orderByDesc(BudgetPlan::getEffectiveAt)
                .orderByDesc(BudgetPlan::getUpdateTime)
                .orderByDesc(BudgetPlan::getCreateTime));
        List<BudgetLedger> ledgers = budgetLedgerService.list(new LambdaQueryWrapper<BudgetLedger>()
                .eq(BudgetLedger::getProjectId, projectId)
                .orderByDesc(BudgetLedger::getOccurredAt)
                .orderByDesc(BudgetLedger::getCreateTime));

        BudgetPlan latestPlan = budgetPlans.isEmpty() ? null : budgetPlans.get(0);
        ProjectBudgetSummaryVO budgetSummary = buildBudgetSummary(latestPlan);

        BudgetCenterDetailVO detail = new BudgetCenterDetailVO();
        detail.setProjectId(project.getId());
        detail.setProjectCode(project.getProjectCode());
        detail.setProjectName(project.getProjectName());
        detail.setProjectType(project.getProjectType());
        detail.setBudgetSummary(budgetSummary);
        detail.setLatestBudgetPlan(toBudgetPlanVO(latestPlan));
        detail.setRoleAllocations(buildRoleAllocations(latestPlan));
        detail.setRecentLedgerEntries(buildBudgetLedgerVOs(limitList(ledgers, DETAIL_LEDGER_LIMIT)));
        return detail;
    }

    private BudgetCenterListVO buildListItem(Project project, BudgetPlan latestPlan) {
        ProjectBudgetSummaryVO budgetSummary = buildBudgetSummary(latestPlan);

        BudgetCenterListVO item = new BudgetCenterListVO();
        item.setProjectId(project.getId());
        item.setProjectCode(project.getProjectCode());
        item.setProjectName(project.getProjectName());
        item.setProjectType(project.getProjectType());
        item.setBudgetPlanId(latestPlan == null ? null : latestPlan.getId());
        item.setPlanName(budgetSummary.getPlanName());
        item.setCurrencyCode(firstNonBlank(budgetSummary.getCurrencyCode(), "TOKEN"));
        item.setBudgetStatus(budgetSummary.getStatus());
        item.setTotalBudgetAmount(budgetSummary.getTotalBudgetAmount());
        item.setLockedAmount(budgetSummary.getLockedAmount());
        item.setConsumedAmount(budgetSummary.getConsumedAmount());
        item.setPendingIncreaseAmount(budgetSummary.getPendingIncreaseAmount());
        item.setLastUpdatedTime(firstNonNull(
                budgetSummary.getLastUpdatedAt(),
                project.getUpdateTime(),
                project.getCreateTime()));
        return item;
    }

    private boolean matchesBudgetStatus(BudgetCenterListVO item, String budgetStatus) {
        if (budgetStatus == null || budgetStatus.isBlank()) {
            return true;
        }
        return Objects.equals(item.getBudgetStatus(), budgetStatus);
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

    private BudgetPlanVO toBudgetPlanVO(BudgetPlan plan) {
        return plan == null ? null : budgetPlanConvert.toVO(plan);
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

    private List<BudgetRoleAllocationVO> buildRoleAllocations(BudgetPlan latestPlan) {
        long totalBudgetAmount = latestPlan == null
                ? 0L
                : zeroIfNull(latestPlan.getApprovedAmount()) > 0L
                        ? zeroIfNull(latestPlan.getApprovedAmount())
                        : zeroIfNull(latestPlan.getProposedAmount());
        List<BudgetRoleAllocationVO> configured = parseRoleAllocations(latestPlan == null ? null : latestPlan.getRoleAllocationsJson(), totalBudgetAmount);
        return configured.isEmpty() ? buildDefaultRoleAllocations(totalBudgetAmount) : configured;
    }

    private List<BudgetRoleAllocationVO> parseRoleAllocations(String json, long totalBudgetAmount) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null || root.isNull()) {
                return List.of();
            }
            if (root.isArray()) {
                List<BudgetRoleAllocationVO> items = new ArrayList<>();
                for (JsonNode node : root) {
                    String roleCode = node.path("roleCode").asText(null);
                    if (roleCode == null || roleCode.isBlank()) {
                        continue;
                    }
                    long amount = node.path("amount").asLong(0L);
                    items.add(roleAllocation(roleCode, amount, totalBudgetAmount));
                }
                return items;
            }
            if (root.isObject()) {
                List<BudgetRoleAllocationVO> items = new ArrayList<>();
                root.fields().forEachRemaining(entry -> {
                    long amount = entry.getValue() == null ? 0L : entry.getValue().asLong(0L);
                    items.add(roleAllocation(entry.getKey(), amount, totalBudgetAmount));
                });
                return items;
            }
        } catch (Exception ignored) {
            return List.of();
        }
        return List.of();
    }

    private List<BudgetRoleAllocationVO> buildDefaultRoleAllocations(long totalBudgetAmount) {
        if (totalBudgetAmount <= 0L) {
            return List.of();
        }
        return List.of(
                roleAllocation(BudgetRoleCode.PRODUCT_ANALYSIS.getCode(), percentAmount(totalBudgetAmount, 12), totalBudgetAmount),
                roleAllocation(BudgetRoleCode.ARCHITECT.getCode(), percentAmount(totalBudgetAmount, 12), totalBudgetAmount),
                roleAllocation(BudgetRoleCode.UI_UX.getCode(), percentAmount(totalBudgetAmount, 10), totalBudgetAmount),
                roleAllocation(BudgetRoleCode.FRONTEND.getCode(), percentAmount(totalBudgetAmount, 18), totalBudgetAmount),
                roleAllocation(BudgetRoleCode.BACKEND.getCode(), percentAmount(totalBudgetAmount, 22), totalBudgetAmount),
                roleAllocation(BudgetRoleCode.QA.getCode(), percentAmount(totalBudgetAmount, 10), totalBudgetAmount),
                roleAllocation(BudgetRoleCode.DEVOPS.getCode(), percentAmount(totalBudgetAmount, 8), totalBudgetAmount),
                roleAllocation(BudgetRoleCode.PROJECT_COORDINATION.getCode(), totalBudgetAmount
                        - percentAmount(totalBudgetAmount, 12)
                        - percentAmount(totalBudgetAmount, 12)
                        - percentAmount(totalBudgetAmount, 10)
                        - percentAmount(totalBudgetAmount, 18)
                        - percentAmount(totalBudgetAmount, 22)
                        - percentAmount(totalBudgetAmount, 10)
                        - percentAmount(totalBudgetAmount, 8), totalBudgetAmount));
    }

    private long percentAmount(long totalBudgetAmount, int percent) {
        return Math.round(totalBudgetAmount * percent / 100.0d);
    }

    private BudgetRoleAllocationVO roleAllocation(String roleCode, long amount, long totalBudgetAmount) {
        BudgetRoleAllocationVO item = new BudgetRoleAllocationVO();
        item.setRoleCode(roleCode);
        item.setRoleName(resolveBudgetRoleLabel(roleCode));
        item.setAmount(amount);
        item.setSharePercent(totalBudgetAmount <= 0L ? 0 : (int) Math.round(amount * 100.0d / totalBudgetAmount));
        return item;
    }

    private String resolveBudgetRoleLabel(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return "-";
        }
        for (BudgetRoleCode item : BudgetRoleCode.values()) {
            if (Objects.equals(item.getCode(), roleCode)) {
                return item.getLabel();
            }
        }
        return roleCode;
    }

    private List<BudgetLedgerVO> buildBudgetLedgerVOs(List<BudgetLedger> ledgers) {
        if (ledgers == null || ledgers.isEmpty()) {
            return List.of();
        }
        List<BudgetLedgerVO> vos = budgetLedgerConvert.toVOs(ledgers);
        Map<String, String> decisionTitleMap = loadDecisionTitleMap(ledgers);
        for (BudgetLedgerVO vo : vos) {
            if (vo == null) {
                continue;
            }
            vo.setReferenceDisplayName(resolveLedgerReferenceDisplayName(vo, decisionTitleMap));
        }
        return vos;
    }

    private Map<String, String> loadDecisionTitleMap(Collection<BudgetLedger> ledgers) {
        List<String> decisionIds = ledgers == null
                ? List.of()
                : ledgers.stream()
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

    private boolean isDecisionReference(BudgetLedger ledger) {
        return ledger != null && isDecisionReferenceType(ledger.getReferenceType());
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

    private <T> List<T> limitList(List<T> items, int limit) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream().limit(limit).toList();
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
}
