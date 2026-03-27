package com.hiking.treasure.service;

import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.mapper.RoleMapper;
import com.hiking.treasure.mapper.RolePermissionMapper;
import com.hiking.treasure.mapper.UserMapper;
import com.hiking.treasure.mapper.UserRoleMapper;
import com.hiking.treasure.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RolePermissionMapper rolePermissionMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private UserMapper userMapper;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(rolePermissionMapper, userRoleMapper, userMapper);
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
    }

    @Test
    void deleteRoleByIdRejectsWhenAssignedToUsers() {
        Role role = new Role();
        role.setId("r1");
        role.setRoleName("平台管理员");
        User user = new User();
        user.setId("u1");
        user.setUsername("admin");
        user.setRealname("管理员");
        UserRole userRole = new UserRole();
        userRole.setRoleId("r1");
        userRole.setUserId("u1");
        when(roleMapper.selectBatchIds(List.of("r1"))).thenReturn(List.of(role));
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(userMapper.selectBatchIds(List.of("u1"))).thenReturn(List.of(user));

        BusinessException exception = assertThrows(BusinessException.class, () -> roleService.deleteRoleById("r1"));
        assertTrue(exception.getMessage().contains("平台管理员"));
        assertTrue(exception.getMessage().contains("管理员(admin)"));
        assertInstanceOf(DeleteConflictResultVO.class, exception.getData());
    }
}
