package com.hiking.treasure.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.mapper.AnnouncementSendMapper;
import com.hiking.treasure.mapper.PermissionDataRuleMapper;
import com.hiking.treasure.mapper.QuartzJobLogMapper;
import com.hiking.treasure.mapper.RolePermissionMapper;
import com.hiking.treasure.mapper.TenantMapper;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.mapper.UserRoleMapper;
import com.hiking.treasure.service.impl.TenantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock private TenantMapper tenantMapper;
    @Mock private UserService userService;
    @Mock private RoleService roleService;
    @Mock private PermissionService permissionService;
    @Mock private DepartService departService;
    @Mock private AnnouncementService announcementService;
    @Mock private FileService fileService;
    @Mock private QuartzJobService quartzJobService;
    @Mock private DictService dictService;
    @Mock private DictItemService dictItemService;
    @Mock private LogService logService;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private UserDepartMapper userDepartMapper;
    @Mock private RolePermissionMapper rolePermissionMapper;
    @Mock private AnnouncementSendMapper announcementSendMapper;
    @Mock private QuartzJobLogMapper quartzJobLogMapper;
    @Mock private PermissionDataRuleMapper permissionDataRuleMapper;

    private TenantServiceImpl tenantService;

    @BeforeEach
    void setUp() {
        tenantService = new TenantServiceImpl(
                userService, roleService, permissionService, departService,
                announcementService, fileService, quartzJobService, dictService,
                dictItemService, logService, userRoleMapper,
                userDepartMapper, rolePermissionMapper, announcementSendMapper,
                quartzJobLogMapper, permissionDataRuleMapper
        );
        ReflectionTestUtils.setField(tenantService, "baseMapper", tenantMapper);
    }

    @Test
    void deleteTenantByIdRejectsWhenUsersExist() {
        User user = new User();
        user.setId("u1");
        user.setUsername("admin");
        user.setRealname("管理员");
        when(userService.count(any())).thenReturn(1L);
        when(userService.list(org.mockito.ArgumentMatchers.<Wrapper<User>>any())).thenReturn(List.of(user));

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantService.deleteTenantById("t1"));
        assertTrue(exception.getMessage().contains("用户"));
        assertTrue(exception.getMessage().contains("管理员(admin)"));
        assertInstanceOf(DeleteConflictResultVO.class, exception.getData());
    }

    @Test
    void deleteTenantByIdRejectsWhenUserRoleRelationExistsWithReadableNames() {
        User user = new User();
        user.setId("u1");
        user.setUsername("admin");
        user.setRealname("管理员");
        Role role = new Role();
        role.setId("r1");
        role.setRoleName("平台管理员");
        UserRole link = new UserRole();
        link.setUserId("u1");
        link.setRoleId("r1");

        when(userService.count(any())).thenReturn(0L);
        when(roleService.count(any())).thenReturn(0L);
        when(permissionService.count(any())).thenReturn(0L);
        when(departService.count(any())).thenReturn(0L);
        when(announcementService.count(any())).thenReturn(0L);
        when(fileService.count(any())).thenReturn(0L);
        when(quartzJobService.count(any())).thenReturn(0L);
        when(dictService.count(any())).thenReturn(0L);
        when(dictItemService.count(any())).thenReturn(0L);
        when(logService.count(any())).thenReturn(0L);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(link));
        when(userService.listByIds(List.of("u1"))).thenReturn(List.of(user));
        when(roleService.listByIds(List.of("r1"))).thenReturn(List.of(role));

        BusinessException exception = assertThrows(BusinessException.class, () -> tenantService.deleteTenantById("t1"));

        assertTrue(exception.getMessage().contains("用户角色关系"));
        assertTrue(exception.getMessage().contains("管理员(admin)"));
        assertTrue(exception.getMessage().contains("平台管理员"));
        assertInstanceOf(DeleteConflictResultVO.class, exception.getData());
    }
}
