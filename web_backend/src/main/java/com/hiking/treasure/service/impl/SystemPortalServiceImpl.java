package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.domain.vo.system.CurrentUserProfileVO;
import com.hiking.treasure.domain.vo.system.DashboardStatsVO;
import com.hiking.treasure.domain.vo.system.MenuTreeVO;
import com.hiking.treasure.entity.Announcement;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.entity.Permission;
import com.hiking.treasure.entity.QuartzJob;
import com.hiking.treasure.entity.Tenant;
import com.hiking.treasure.entity.User;
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
        return vo;
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
