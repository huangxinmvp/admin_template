package com.hiking.treasure.controller;

import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.exception.GlobalExceptionHandler;
import com.hiking.treasure.domain.vo.system.DeleteConflictDetailVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictItemVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.service.DepartService;
import com.hiking.treasure.service.PermissionService;
import com.hiking.treasure.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DeleteConflictContractMvcTest {

    @Mock
    private PermissionService permissionService;
    @Mock
    private RoleService roleService;
    @Mock
    private DepartService departService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PermissionController permissionController = new PermissionController();
        ReflectionTestUtils.setField(permissionController, "permissionService", permissionService);

        RoleController roleController = new RoleController();
        ReflectionTestUtils.setField(roleController, "roleService", roleService);

        DepartController departController = new DepartController();
        ReflectionTestUtils.setField(departController, "departService", departService);

        mockMvc = MockMvcBuilders.standaloneSetup(permissionController, roleController, departController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void permissionDeleteReturnsStructuredConflictPayload() throws Exception {
        when(permissionService.deletePermissionById("p1")).thenThrow(new BusinessException(
                400,
                "菜单删除失败，以下菜单仍被角色授权: 租户管理(平台管理员)",
                DeleteConflictResultVO.of("permission", List.of(
                        DeleteConflictDetailVO.of("assigned_role", "角色授权",
                                List.of(DeleteConflictItemVO.of("p1", "租户管理(平台管理员)", "permission",
                                        Map.of("title", "关联对象",
                                                "summary", "共 1 个角色",
                                                "relatedType", "role",
                                                "relatedItems", List.of(DeleteConflictItemVO.of("r1", "平台管理员", "role"))))))
                ))));

        mockMvc.perform(delete("/api/permission/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("菜单删除失败，以下菜单仍被角色授权: 租户管理(平台管理员)"))
                .andExpect(jsonPath("$.result.entityType").value("permission"))
                .andExpect(jsonPath("$.result.details[0].code").value("assigned_role"))
                .andExpect(jsonPath("$.result.details[0].label").value("角色授权"))
                .andExpect(jsonPath("$.result.details[0].items[0].id").value("p1"))
                .andExpect(jsonPath("$.result.details[0].items[0].name").value("租户管理(平台管理员)"))
                .andExpect(jsonPath("$.result.details[0].items[0].type").value("permission"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.title").value("关联对象"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.summary").value("共 1 个角色"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.relatedType").value("role"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.relatedItems[0].id").value("r1"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.relatedItems[0].name").value("平台管理员"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.relatedItems[0].type").value("role"));
    }

    @Test
    void roleDeleteReturnsStructuredConflictPayload() throws Exception {
        when(roleService.deleteRoleById("r1")).thenThrow(new BusinessException(
                400,
                "角色删除失败，以下角色仍被用户占用: 平台管理员(管理员(admin))",
                DeleteConflictResultVO.of("role", List.of(
                        DeleteConflictDetailVO.of("assigned_user", "关联用户",
                                List.of(DeleteConflictItemVO.of("r1", "平台管理员(管理员(admin))", "role",
                                        Map.of("title", "关联用户",
                                                "summary", "共 1 个用户",
                                                "relatedType", "user",
                                                "relatedItems", List.of(DeleteConflictItemVO.of("u1", "管理员(admin)", "user"))))))
                ))));

        mockMvc.perform(delete("/api/role/r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.result.entityType").value("role"))
                .andExpect(jsonPath("$.result.details[0].code").value("assigned_user"))
                .andExpect(jsonPath("$.result.details[0].items[0].id").value("r1"))
                .andExpect(jsonPath("$.result.details[0].items[0].name").value("平台管理员(管理员(admin))"))
                .andExpect(jsonPath("$.result.details[0].items[0].type").value("role"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.title").value("关联用户"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.summary").value("共 1 个用户"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.relatedType").value("user"));
    }

    @Test
    void departDeleteReturnsStructuredConflictPayload() throws Exception {
        when(departService.deleteDepartById("d1")).thenThrow(new BusinessException(
                400,
                "部门删除失败，以下部门仍有子部门: 总部(研发部)",
                DeleteConflictResultVO.of("depart", List.of(
                        DeleteConflictDetailVO.of("child_depart", "子部门",
                                List.of(DeleteConflictItemVO.of("d1", "总部(研发部)", "depart",
                                        Map.of("title", "关联子部门",
                                                "summary", "共 1 个子部门",
                                                "relatedType", "depart",
                                                "relatedItems", List.of(DeleteConflictItemVO.of("d2", "研发部", "depart"))))))
                ))));

        mockMvc.perform(delete("/api/depart/d1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.result.entityType").value("depart"))
                .andExpect(jsonPath("$.result.details[0].code").value("child_depart"))
                .andExpect(jsonPath("$.result.details[0].items[0].id").value("d1"))
                .andExpect(jsonPath("$.result.details[0].items[0].name").value("总部(研发部)"))
                .andExpect(jsonPath("$.result.details[0].items[0].type").value("depart"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.title").value("关联子部门"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.summary").value("共 1 个子部门"))
                .andExpect(jsonPath("$.result.details[0].items[0].meta.relatedType").value("depart"));
    }
}
