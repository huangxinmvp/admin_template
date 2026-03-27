package com.hiking.treasure.service;

import com.hiking.treasure.common.security.LoginUser;
import com.hiking.treasure.domain.vo.system.MenuTreeVO;
import com.hiking.treasure.entity.Permission;
import com.hiking.treasure.entity.Tenant;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.service.impl.SystemPortalServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemPortalServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private TenantService tenantService;
    @Mock
    private RoleService roleService;
    @Mock
    private DepartService departService;
    @Mock
    private AnnouncementService announcementService;
    @Mock
    private QuartzJobService quartzJobService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserMenusBuildsSortedTree() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new LoginUser("u1", "admin", "t1", List.of("ADMIN")), null, List.of())
        );

        Permission root = new Permission();
        root.setId("1");
        root.setName("System");
        root.setSortNo(2);
        root.setType(0);

        Permission childB = new Permission();
        childB.setId("3");
        childB.setParentId("1");
        childB.setName("Users");
        childB.setSortNo(2);
        childB.setType(1);

        Permission childA = new Permission();
        childA.setId("2");
        childA.setParentId("1");
        childA.setName("Roles");
        childA.setSortNo(1);
        childA.setType(1);

        when(permissionService.listMenuByUserId("u1")).thenReturn(List.of(childB, root, childA));

        SystemPortalServiceImpl service = new SystemPortalServiceImpl(
                userService, permissionService, tenantService, roleService, departService, announcementService, quartzJobService
        );

        List<MenuTreeVO> menus = service.getCurrentUserMenus();

        assertEquals(1, menus.size());
        assertEquals("System", menus.get(0).getName());
        assertIterableEquals(List.of("Roles", "Users"), menus.get(0).getChildren().stream().map(MenuTreeVO::getName).toList());
    }

    @Test
    void getCurrentUserProfileIncludesTenantAndPermissions() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new LoginUser("u1", "admin", "t1", List.of("ADMIN")), null, List.of())
        );

        User user = new User();
        user.setId("u1");
        user.setTenantId("t1");
        user.setUsername("admin");
        user.setRealname("Admin");

        Tenant tenant = new Tenant();
        tenant.setTenantCode("acme");
        tenant.setTenantName("Acme");

        when(userService.getById("u1")).thenReturn(user);
        when(userService.getRoleCodes("u1")).thenReturn(List.of("ADMIN"));
        when(userService.getDepartIds("u1")).thenReturn(List.of("d1"));
        when(permissionService.listPermissionCodesByUserId("u1")).thenReturn(List.of("sys:dashboard:view"));
        when(tenantService.requireActiveTenant("t1")).thenReturn(tenant);

        SystemPortalServiceImpl service = new SystemPortalServiceImpl(
                userService, permissionService, tenantService, roleService, departService, announcementService, quartzJobService
        );

        var profile = service.getCurrentUserProfile();

        assertEquals("acme", profile.getTenantCode());
        assertEquals("Acme", profile.getTenantName());
        assertIterableEquals(List.of("sys:dashboard:view"), profile.getPermissions());
    }
}
