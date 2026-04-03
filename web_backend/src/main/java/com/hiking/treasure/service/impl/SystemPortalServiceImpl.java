package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.domain.vo.system.CurrentUserProfileVO;
import com.hiking.treasure.domain.vo.system.DashboardAttentionProjectVO;
import com.hiking.treasure.domain.vo.system.DashboardBottleneckSummaryVO;
import com.hiking.treasure.domain.vo.system.DashboardBudgetHealthVO;
import com.hiking.treasure.domain.vo.system.DashboardOverviewVO;
import com.hiking.treasure.domain.vo.system.DashboardRecentActivityVO;
import com.hiking.treasure.domain.vo.system.DashboardStageDistributionVO;
import com.hiking.treasure.domain.vo.system.DashboardStatsVO;
import com.hiking.treasure.domain.vo.system.MenuTreeVO;
import com.hiking.treasure.entity.Announcement;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.entity.Permission;
import com.hiking.treasure.entity.QuartzJob;
import com.hiking.treasure.entity.Tenant;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectActivityVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterDetailVO;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetHealthStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectStatus;
import com.hiking.treasure.modules.phase1.service.ProjectService;
import com.hiking.treasure.service.AnnouncementService;
import com.hiking.treasure.service.DepartService;
import com.hiking.treasure.service.PermissionService;
import com.hiking.treasure.service.QuartzJobService;
import com.hiking.treasure.service.RoleService;
import com.hiking.treasure.service.SystemPortalService;
import com.hiking.treasure.service.TenantService;
import com.hiking.treasure.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemPortalServiceImpl implements SystemPortalService {

    private final UserService userService;
    private final PermissionService permissionService;
    private final TenantService tenantService;
    private final RoleService roleService;
    private final DepartService departService;
    private final AnnouncementService announcementService;
    private final QuartzJobService quartzJobService;
    private final ProjectService projectService;

    @Override
    public CurrentUserProfileVO getCurrentUserProfile() {
        String userId = SecurityUtils.getRequiredUserId();
        User user = userService.getById(userId);
        CurrentUserProfileVO vo = new CurrentUserProfileVO();
        vo.setUserId(user.getId());
        vo.setTenantId(user.getTenantId());
        vo.setUsername(user.getUsername());
        vo.setRealname(user.getRealname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRoles(userService.getRoleCodes(userId));
        vo.setDepts(userService.getDepartIds(userId));
        vo.setPermissions(permissionService.listPermissionCodesByUserId(userId));
        Tenant tenant = tenantService.requireActiveTenant(user.getTenantId());
        if (tenant != null) {
            vo.setTenantCode(tenant.getTenantCode());
            vo.setTenantName(tenant.getTenantName());
        }
        return vo;
    }

    @Override
    public List<MenuTreeVO> getCurrentUserMenus() {
        String userId = SecurityUtils.getRequiredUserId();
        return buildMenuTree(permissionService.listMenuByUserId(userId));
    }

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setTenantCount(tenantService.count());
        vo.setUserCount(userService.count());
        vo.setEnabledUserCount(userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        vo.setRoleCount(roleService.count());
        vo.setPermissionCount(permissionService.count());
        vo.setDepartCount(departService.count(new LambdaQueryWrapper<Depart>().eq(Depart::getStatus, 1)));
        vo.setAnnouncementCount(announcementService.count(new LambdaQueryWrapper<Announcement>().eq(Announcement::getSendStatus, 1)));
        vo.setQuartzJobCount(quartzJobService.count(new LambdaQueryWrapper<QuartzJob>().eq(QuartzJob::getStatus, 1)));
        List<ProjectCenterDetailVO> projectDetails = loadProjectDetails();
        vo.setOverview(buildOverview(projectDetails));
        vo.setStageDistribution(buildStageDistribution(projectDetails));
        vo.setBottleneckSummary(buildBottleneckSummary(projectDetails));
        vo.setBudgetHealth(buildBudgetHealth(projectDetails));
        vo.setAttentionProjects(buildAttentionProjects(projectDetails));
        vo.setRecentGovernanceActivities(buildRecentGovernanceActivities(projectDetails));
        return vo;
    }

    private List<ProjectCenterDetailVO> loadProjectDetails() {
        List<Project> projects = projectService.list(new LambdaQueryWrapper<Project>()
                .orderByDesc(Project::getUpdateTime)
                .orderByDesc(Project::getCreateTime));
        if (projects.isEmpty()) {
            return List.of();
        }
        return projects.stream()
                .map(Project::getId)
                .filter(Objects::nonNull)
                .map(projectService::getProjectCenterDetail)
                .toList();
    }

    private DashboardOverviewVO buildOverview(List<ProjectCenterDetailVO> projectDetails) {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setTotalProjectCount(projectDetails.size());
        vo.setActiveProjectCount(projectDetails.stream()
                .filter(detail -> Objects.equals(
                        detail.getProject().getStatus(),
                        ProjectStatus.ACTIVE.getCode()))
                .count());
        vo.setPendingDecisionCount(projectDetails.stream()
                .mapToLong(detail -> zero(detail.getGovernanceSummary().getPendingDecisionItemCount()))
                .sum());
        vo.setPendingApprovalCount(projectDetails.stream()
                .mapToLong(detail -> zero(detail.getGovernanceSummary().getPendingApprovalCount()))
                .sum());
        vo.setBlockedProjectCount(projectDetails.stream()
                .filter(this::isBlockedProject)
                .count());
        vo.setHighRiskProjectCount(projectDetails.stream()
                .filter(this::isHighRiskProject)
                .count());
        vo.setBudgetWarningProjectCount(projectDetails.stream()
                .filter(detail -> Objects.equals(
                        detail.getBudgetSummary().getStatus(),
                        BudgetHealthStatus.WARNING.getCode()))
                .count());
        vo.setBudgetExceededProjectCount(projectDetails.stream()
                .filter(detail -> Objects.equals(
                        detail.getBudgetSummary().getStatus(),
                        BudgetHealthStatus.OVERRUN.getCode()))
                .count());
        return vo;
    }

    private List<DashboardStageDistributionVO> buildStageDistribution(List<ProjectCenterDetailVO> projectDetails) {
        Map<String, Long> countByStageCode = projectDetails.stream()
                .collect(Collectors.groupingBy(
                        detail -> firstNonBlank(
                                detail.getGovernanceSummary().getCurrentStageCode(),
                                detail.getProject().getCurrentStageCode(),
                                "unknown"),
                        LinkedHashMap::new,
                        Collectors.counting()));

        return countByStageCode.entrySet().stream()
                .map(entry -> {
                    DashboardStageDistributionVO item = new DashboardStageDistributionVO();
                    item.setStageCode(entry.getKey());
                    item.setStageName(firstNonBlank(
                            findStageName(projectDetails, entry.getKey()),
                            entry.getKey()));
                    item.setProjectCount(entry.getValue());
                    return item;
                })
                .sorted(Comparator
                        .comparingLong(DashboardStageDistributionVO::getProjectCount)
                        .reversed()
                        .thenComparing(DashboardStageDistributionVO::getStageName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private DashboardBottleneckSummaryVO buildBottleneckSummary(List<ProjectCenterDetailVO> projectDetails) {
        DashboardBottleneckSummaryVO vo = new DashboardBottleneckSummaryVO();
        vo.setBlockerClarificationProjectCount(projectDetails.stream()
                .filter(detail -> zero(detail.getRequirementSummary().getBlockerCount()) > 0)
                .count());
        vo.setPendingDecisionProjectCount(projectDetails.stream()
                .filter(detail -> zero(detail.getGovernanceSummary().getPendingDecisionItemCount()) > 0)
                .count());
        vo.setBlockingApprovalProjectCount(projectDetails.stream()
                .filter(detail -> zero(detail.getGovernanceSummary().getBlockerApprovalCount()) > 0)
                .count());
        vo.setBudgetWarningOrExceededProjectCount(projectDetails.stream()
                .filter(this::hasBudgetWarningOrExceeded)
                .count());
        vo.setMissingCriticalRoleProjectCount(projectDetails.stream()
                .filter(detail -> detail.getMissingCriticalRoles() != null && !detail.getMissingCriticalRoles().isEmpty())
                .count());
        vo.setFailedGateConditionProjectCount(projectDetails.stream()
                .filter(this::hasFailedGateCondition)
                .count());
        return vo;
    }

    private DashboardBudgetHealthVO buildBudgetHealth(List<ProjectCenterDetailVO> projectDetails) {
        DashboardBudgetHealthVO vo = new DashboardBudgetHealthVO();
        vo.setUnplannedProjectCount(countByBudgetStatus(projectDetails, BudgetHealthStatus.UNPLANNED.getCode()));
        vo.setPendingProjectCount(countByBudgetStatus(projectDetails, BudgetHealthStatus.PENDING.getCode()));
        vo.setHealthyProjectCount(countByBudgetStatus(projectDetails, BudgetHealthStatus.HEALTHY.getCode()));
        vo.setWarningProjectCount(countByBudgetStatus(projectDetails, BudgetHealthStatus.WARNING.getCode()));
        vo.setOverrunProjectCount(countByBudgetStatus(projectDetails, BudgetHealthStatus.OVERRUN.getCode()));
        return vo;
    }

    private List<DashboardAttentionProjectVO> buildAttentionProjects(List<ProjectCenterDetailVO> projectDetails) {
        return projectDetails.stream()
                .filter(this::needsAttention)
                .map(detail -> {
                    DashboardAttentionProjectVO item = new DashboardAttentionProjectVO();
                    item.setProjectId(detail.getProject().getId());
                    item.setProjectCode(detail.getProject().getProjectCode());
                    item.setProjectName(detail.getProject().getProjectName());
                    item.setCurrentStageCode(firstNonBlank(
                            detail.getGovernanceSummary().getCurrentStageCode(),
                            detail.getProject().getCurrentStageCode()));
                    item.setCurrentStageName(firstNonBlank(
                            detail.getGovernanceSummary().getCurrentStageName(),
                            item.getCurrentStageCode()));
                    item.setRiskLevel(detail.getProject().getRiskLevel());
                    item.setBlockingReason(buildBlockingReason(detail));
                    item.setBudgetStatus(detail.getBudgetSummary().getStatus());
                    item.setPendingApprovalCount(zero(detail.getGovernanceSummary().getPendingApprovalCount()));
                    item.setPendingDecisionCount(zero(detail.getGovernanceSummary().getPendingDecisionItemCount()));
                    item.setUpdateTime(firstNonNull(
                            detail.getProject().getUpdateTime(),
                            detail.getProject().getCreateTime()));
                    return item;
                })
                .sorted(Comparator
                        .comparingInt((DashboardAttentionProjectVO item) -> attentionScore(item)).reversed()
                        .thenComparing(DashboardAttentionProjectVO::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .toList();
    }

    private List<DashboardRecentActivityVO> buildRecentGovernanceActivities(List<ProjectCenterDetailVO> projectDetails) {
        return projectDetails.stream()
                .flatMap(detail -> safe(detail.getRecentActivities()).stream()
                        .map(activity -> toDashboardRecentActivity(detail, activity)))
                .sorted(Comparator.comparing(
                        DashboardRecentActivityVO::getOccurredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(16)
                .toList();
    }

    private DashboardRecentActivityVO toDashboardRecentActivity(
            ProjectCenterDetailVO detail,
            ProjectActivityVO activity) {
        DashboardRecentActivityVO item = new DashboardRecentActivityVO();
        item.setProjectId(detail.getProject().getId());
        item.setProjectName(firstNonBlank(
                detail.getProject().getProjectName(),
                detail.getProject().getProjectCode(),
                detail.getProject().getId()));
        item.setActivityType(activity.getActivityType());
        item.setTitle(activity.getTitle());
        item.setDescription(activity.getDescription());
        item.setStatus(activity.getStatus());
        item.setOccurredAt(activity.getOccurredAt());
        return item;
    }

    private String findStageName(List<ProjectCenterDetailVO> projectDetails, String stageCode) {
        return projectDetails.stream()
                .filter(detail -> Objects.equals(detail.getGovernanceSummary().getCurrentStageCode(), stageCode))
                .map(detail -> detail.getGovernanceSummary().getCurrentStageName())
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .findFirst()
                .orElse(stageCode);
    }

    private long countByBudgetStatus(List<ProjectCenterDetailVO> projectDetails, String status) {
        return projectDetails.stream()
                .filter(detail -> Objects.equals(detail.getBudgetSummary().getStatus(), status))
                .count();
    }

    private boolean needsAttention(ProjectCenterDetailVO detail) {
        return isBlockedProject(detail)
                || isAtRiskProject(detail)
                || isHighRiskProject(detail)
                || hasBudgetWarningOrExceeded(detail)
                || zero(detail.getGovernanceSummary().getPendingApprovalCount()) > 0
                || zero(detail.getGovernanceSummary().getPendingDecisionItemCount()) > 0;
    }

    private boolean isBlockedProject(ProjectCenterDetailVO detail) {
        return zero(detail.getGovernanceSummary().getBlockedFlag()) > 0
                || Objects.equals(detail.getGovernanceSummary().getCurrentStageStatus(), "blocked")
                || zero(detail.getRequirementSummary().getBlockerCount()) > 0
                || zero(detail.getGovernanceSummary().getBlockerDecisionCount()) > 0
                || zero(detail.getGovernanceSummary().getBlockerApprovalCount()) > 0
                || zero(detail.getGovernanceSummary().getBlockingGateConditionCount()) > 0
                || (detail.getMissingCriticalRoles() != null && !detail.getMissingCriticalRoles().isEmpty());
    }

    private boolean isAtRiskProject(ProjectCenterDetailVO detail) {
        return zero(detail.getGovernanceSummary().getAtRiskFlag()) > 0;
    }

    private boolean isHighRiskProject(ProjectCenterDetailVO detail) {
        return Objects.equals(detail.getProject().getRiskLevel(), "high")
                || Objects.equals(detail.getProject().getRiskLevel(), "critical");
    }

    private boolean hasBudgetWarningOrExceeded(ProjectCenterDetailVO detail) {
        return Objects.equals(detail.getBudgetSummary().getStatus(), BudgetHealthStatus.WARNING.getCode())
                || Objects.equals(detail.getBudgetSummary().getStatus(), BudgetHealthStatus.OVERRUN.getCode());
    }

    private boolean hasFailedGateCondition(ProjectCenterDetailVO detail) {
        return zero(detail.getGovernanceSummary().getFailedGateConditionCount()) > 0
                || zero(detail.getGovernanceSummary().getBlockingGateConditionCount()) > 0
                || safe(detail.getCurrentGateConditions()).stream()
                .anyMatch(condition -> Objects.equals(condition.getStatus(), "blocked"));
    }

    private String buildBlockingReason(ProjectCenterDetailVO detail) {
        if (detail.getGovernanceSummary().getBlockerReasonSummary() != null
                && !detail.getGovernanceSummary().getBlockerReasonSummary().isBlank()) {
            return detail.getGovernanceSummary().getBlockerReasonSummary();
        }
        List<String> reasons = new ArrayList<>();
        if (zero(detail.getRequirementSummary().getBlockerCount()) > 0) {
            reasons.add("阻塞澄清项");
        }
        if (zero(detail.getGovernanceSummary().getBlockerDecisionCount()) > 0) {
            reasons.add("阻塞决策");
        }
        if (zero(detail.getGovernanceSummary().getBlockerApprovalCount()) > 0) {
            reasons.add("阻塞审批");
        }
        if (hasBudgetWarningOrExceeded(detail)) {
            reasons.add(Objects.equals(detail.getBudgetSummary().getStatus(), BudgetHealthStatus.OVERRUN.getCode())
                    ? "预算超支"
                    : "预算预警");
        }
        if (detail.getMissingCriticalRoles() != null && !detail.getMissingCriticalRoles().isEmpty()) {
            reasons.add("关键角色缺失");
        }
        if (hasFailedGateCondition(detail)) {
            reasons.add("门禁条件阻塞");
        }
        if (reasons.isEmpty() && isHighRiskProject(detail)) {
            reasons.add("高风险项目");
        }
        if (reasons.isEmpty() && zero(detail.getGovernanceSummary().getPendingApprovalCount()) > 0) {
            reasons.add("待处理审批");
        }
        if (reasons.isEmpty() && zero(detail.getGovernanceSummary().getPendingDecisionItemCount()) > 0) {
            reasons.add("待处理决策");
        }
        return reasons.isEmpty() ? "需要关注" : reasons.stream().limit(2).collect(Collectors.joining(" / "));
    }

    private int attentionScore(DashboardAttentionProjectVO item) {
        int score = 0;
        if (item.getBlockingReason() != null && !item.getBlockingReason().isBlank()) {
            score += 1;
        }
        if (Objects.equals(item.getRiskLevel(), "critical")) {
            score += 5;
        } else if (Objects.equals(item.getRiskLevel(), "high")) {
            score += 3;
        }
        if (Objects.equals(item.getBudgetStatus(), BudgetHealthStatus.OVERRUN.getCode())) {
            score += 4;
        } else if (Objects.equals(item.getBudgetStatus(), BudgetHealthStatus.WARNING.getCode())) {
            score += 2;
        }
        score += Math.min(zero(item.getPendingApprovalCount()), 3);
        score += Math.min(zero(item.getPendingDecisionCount()), 3);
        return score;
    }

    private int zero(Integer value) {
        return value == null ? 0 : value;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
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

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private List<MenuTreeVO> buildMenuTree(List<Permission> permissions) {
        Map<String, MenuTreeVO> nodeMap = new LinkedHashMap<>();
        List<MenuTreeVO> roots = new ArrayList<>();
        for (Permission permission : permissions) {
            MenuTreeVO node = new MenuTreeVO();
            node.setId(permission.getId());
            node.setParentId(permission.getParentId());
            node.setName(permission.getName());
            node.setPath(permission.getUrl());
            node.setComponent(permission.getComponent());
            node.setPerms(permission.getPerms());
            node.setType(permission.getType());
            node.setSortNo(permission.getSortNo());
            node.setHidden(permission.getHidden());
            node.setAlwaysShow(permission.getAlwaysShow());
            node.setIcon(permission.getIcon());
            nodeMap.put(node.getId(), node);
        }
        for (MenuTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId().isBlank() || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        Comparator<MenuTreeVO> comparator = Comparator.comparing(MenuTreeVO::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuTreeVO::getName, Comparator.nullsLast(String::compareTo));
        sortNodes(roots, comparator);
        return roots;
    }

    private void sortNodes(List<MenuTreeVO> nodes, Comparator<MenuTreeVO> comparator) {
        nodes.sort(comparator);
        for (MenuTreeVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortNodes(node.getChildren(), comparator);
            }
        }
    }
}
