package com.hiking.treasure.service;

import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.domain.dto.PermissionTreeSaveDTO;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.domain.vo.system.PermissionTreeSaveResultVO;
import com.hiking.treasure.entity.Permission;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.entity.RolePermission;
import com.hiking.treasure.mapper.PermissionDataRuleMapper;
import com.hiking.treasure.mapper.PermissionMapper;
import com.hiking.treasure.mapper.RoleMapper;
import com.hiking.treasure.mapper.RolePermissionMapper;
import com.hiking.treasure.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RolePermissionMapper rolePermissionMapper;
    @Mock
    private PermissionDataRuleMapper permissionDataRuleMapper;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(roleMapper, rolePermissionMapper, permissionDataRuleMapper);
        ReflectionTestUtils.setField(permissionService, "baseMapper", permissionMapper);
    }

    @Test
    void saveTreeUpdatesParentAndSortNoAndReturnsLatestTree() {
        Permission root = new Permission();
        root.setId("p-root");
        root.setName("系统");

        Permission child = new Permission();
        child.setId("p-child");
        child.setName("用户");

        when(permissionMapper.selectById("p-root")).thenReturn(root);
        when(permissionMapper.selectById("p-child")).thenReturn(child);
        when(permissionMapper.updateById(any(Permission.class))).thenReturn(1);

        Permission savedRoot = new Permission();
        savedRoot.setId("p-root");
        savedRoot.setName("系统");
        savedRoot.setSortNo(10);
        Permission savedChild = new Permission();
        savedChild.setId("p-child");
        savedChild.setParentId("p-root");
        savedChild.setName("用户");
        savedChild.setSortNo(10);
        when(permissionMapper.selectList(any())).thenReturn(List.of(savedRoot, savedChild));

        PermissionTreeSaveDTO childDto = new PermissionTreeSaveDTO();
        childDto.setId("p-child");
        PermissionTreeSaveDTO rootDto = new PermissionTreeSaveDTO();
        rootDto.setId("p-root");
        rootDto.setChildren(List.of(childDto));

        PermissionTreeSaveResultVO result = permissionService.saveTree(List.of(rootDto));

        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionMapper, atLeastOnce()).updateById(captor.capture());
        List<Permission> updates = captor.getAllValues();
        assertEquals("p-root", updates.get(0).getId());
        assertEquals(10, updates.get(0).getSortNo());
        assertEquals("p-child", updates.get(1).getId());
        assertEquals("p-root", updates.get(1).getParentId());
        assertEquals(2, result.getUpdatedCount());
        assertNotNull(result.getTree());
        assertEquals(1, result.getTree().size());
        assertEquals("p-root", result.getTree().get(0).getId());
    }

    @Test
    void savePermissionRejectsDuplicateRoute() {
        Permission duplicated = new Permission();
        duplicated.setId("p-exists");
        duplicated.setUrl("/system/user");
        duplicated.setType(1);

        Permission create = new Permission();
        create.setId("p-new");
        create.setUrl("/system/user");
        create.setType(1);

        doReturn(duplicated).when(permissionMapper).selectOne(any(), org.mockito.ArgumentMatchers.eq(true));

        assertThrows(BusinessException.class, () -> permissionService.savePermission(create));

        verify(permissionMapper, never()).insert(org.mockito.ArgumentMatchers.<Permission>any());
    }

    @Test
    void deletePermissionByIdRejectsWhenHasChildren() {
        Permission parent = new Permission();
        parent.setId("p1");
        parent.setName("系统管理");
        Permission child = new Permission();
        child.setId("p2");
        child.setParentId("p1");
        child.setName("用户管理");

        when(permissionMapper.selectBatchIds(List.of("p1"))).thenReturn(List.of(parent));
        when(permissionMapper.selectList(any())).thenReturn(List.of(child));

        BusinessException exception = assertThrows(BusinessException.class, () -> permissionService.deletePermissionById("p1"));
        assertTrue(exception.getMessage().contains("系统管理"));
        assertTrue(exception.getMessage().contains("用户管理"));
        assertInstanceOf(DeleteConflictResultVO.class, exception.getData());
    }

    @Test
    void deletePermissionByIdRejectsWhenAssignedToRole() {
        Permission permission = new Permission();
        permission.setId("p1");
        permission.setName("租户管理");
        Role role = new Role();
        role.setId("r1");
        role.setRoleName("平台管理员");
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId("r1");
        rolePermission.setPermissionId("p1");

        when(permissionMapper.selectBatchIds(List.of("p1"))).thenReturn(List.of(permission));
        when(permissionMapper.selectList(any())).thenReturn(List.of());
        when(roleMapper.selectBatchIds(List.of("r1"))).thenReturn(List.of(role));
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of(rolePermission));

        BusinessException exception = assertThrows(BusinessException.class, () -> permissionService.deletePermissionById("p1"));
        assertTrue(exception.getMessage().contains("租户管理"));
        assertTrue(exception.getMessage().contains("平台管理员"));
        assertInstanceOf(DeleteConflictResultVO.class, exception.getData());
    }
}
