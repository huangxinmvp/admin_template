package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.domain.vo.system.DeleteConflictDetailVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictItemVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.domain.vo.system.OptionVO;
import com.hiking.treasure.domain.vo.system.RoleTreeVO;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.entity.RolePermission;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.mapper.RoleMapper;
import com.hiking.treasure.mapper.RolePermissionMapper;
import com.hiking.treasure.mapper.UserMapper;
import com.hiking.treasure.mapper.UserRoleMapper;
import com.hiking.treasure.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;

    @Override
    public List<String> getPermissionIds(String roleId) {
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId))
                .stream()
                .map(RolePermission::getPermissionId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePermissionIds(String roleId, List<String> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        for (String permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }
        return true;
    }

    @Override
    public List<RoleTreeVO> listTree() {
        return list(new LambdaQueryWrapper<Role>()
                .orderByAsc(Role::getRoleName))
                .stream()
                .map(role -> {
                    RoleTreeVO node = new RoleTreeVO();
                    node.setId(role.getId());
                    node.setRoleName(role.getRoleName());
                    node.setRoleCode(role.getRoleCode());
                    node.setStatus(role.getStatus());
                    return node;
                })
                .sorted(Comparator.comparing(RoleTreeVO::getRoleName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    @Override
    public List<OptionVO> listOptions() {
        return list(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 1)
                .orderByAsc(Role::getRoleName))
                .stream()
                .map(role -> {
                    OptionVO option = new OptionVO();
                    option.setValue(role.getId());
                    option.setLabel(role.getRoleName());
                    option.setCode(role.getRoleCode());
                    return option;
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleById(String roleId) {
        validateDeletable(List.of(roleId));
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        return removeById(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRolesByIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        validateDeletable(roleIds);
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds));
        return removeByIds(roleIds);
    }

    private void validateDeletable(List<String> roleIds) {
        Map<String, String> roleNameMap = baseMapper.selectBatchIds(roleIds).stream()
                .collect(java.util.stream.Collectors.toMap(Role::getId, this::getRoleLabel, (left, right) -> left, LinkedHashMap::new));
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().in(UserRole::getRoleId, roleIds));
        if (!userRoles.isEmpty()) {
            Map<String, String> userNameMap = userMapper.selectBatchIds(userRoles.stream()
                            .map(UserRole::getUserId)
                            .filter(StringUtils::hasText)
                            .distinct()
                            .toList()).stream()
                    .collect(java.util.stream.Collectors.toMap(User::getId, this::getUserLabel, (left, right) -> left, LinkedHashMap::new));
            Map<String, List<String>> assignmentDetail = new LinkedHashMap<>();
            for (UserRole userRole : userRoles) {
                assignmentDetail.computeIfAbsent(userRole.getRoleId(), key -> new java.util.ArrayList<>())
                        .add(userNameMap.getOrDefault(userRole.getUserId(), userRole.getUserId()));
            }
            List<DeleteConflictItemVO> items = assignmentDetail.entrySet().stream()
                    .map(entry -> DeleteConflictItemVO.of(
                            entry.getKey(),
                            roleNameMap.getOrDefault(entry.getKey(), entry.getKey()) + "(" + String.join("、", entry.getValue()) + ")",
                            "role",
                            buildRelatedMeta(entry.getValue(), "user")
                    ))
                    .toList();
            throw new BusinessException(400,
                    "角色删除失败，以下角色仍被用户占用: " + items.stream().map(DeleteConflictItemVO::getName).collect(java.util.stream.Collectors.joining("；")),
                    DeleteConflictResultVO.of("role", List.of(
                            DeleteConflictDetailVO.of("assigned_user", "关联用户", items)
                    )));
        }
    }

    private String getRoleLabel(Role role) {
        if (role == null) {
            return "";
        }
        if (StringUtils.hasText(role.getRoleName())) {
            return role.getRoleName();
        }
        if (StringUtils.hasText(role.getRoleCode())) {
            return role.getRoleCode();
        }
        return role.getId();
    }

    private String getUserLabel(User user) {
        if (user == null) {
            return "";
        }
        if (StringUtils.hasText(user.getRealname()) && StringUtils.hasText(user.getUsername())) {
            return user.getRealname() + "(" + user.getUsername() + ")";
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return user.getId();
    }

    private Map<String, Object> buildRelatedMeta(List<String> relatedNames, String relatedType) {
        if (relatedNames == null || relatedNames.isEmpty()) {
            return null;
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", "关联用户");
        meta.put("summary", "共 " + relatedNames.size() + " 个用户");
        meta.put("relatedType", relatedType);
        meta.put("relatedItems", relatedNames.stream()
                .map(name -> DeleteConflictItemVO.of(null, name, relatedType))
                .toList());
        return meta;
    }
}
