package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.domain.dto.PermissionSortDTO;
import com.hiking.treasure.domain.dto.PermissionTreeSaveDTO;
import com.hiking.treasure.domain.vo.system.DeleteConflictDetailVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictItemVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.domain.vo.system.PermissionTreeSaveResultVO;
import com.hiking.treasure.domain.vo.system.PermissionTreeVO;
import com.hiking.treasure.entity.Permission;
import com.hiking.treasure.entity.PermissionDataRule;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.entity.RolePermission;
import com.hiking.treasure.mapper.PermissionDataRuleMapper;
import com.hiking.treasure.mapper.PermissionMapper;
import com.hiking.treasure.mapper.RoleMapper;
import com.hiking.treasure.mapper.RolePermissionMapper;
import com.hiking.treasure.service.PermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 权限表(菜单/按钮) 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionDataRuleMapper permissionDataRuleMapper;

    @Override
    public boolean savePermission(Permission permission) {
        validateRouteUnique(permission);
        return save(permission);
    }

    @Override
    public boolean updatePermission(Permission permission) {
        validateRouteUnique(permission);
        return updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermissionById(String permissionId) {
        validateDeletable(List.of(permissionId));
        permissionDataRuleMapper.delete(new LambdaQueryWrapper<PermissionDataRule>().eq(PermissionDataRule::getPermissionId, permissionId));
        return removeById(permissionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermissionsByIds(List<String> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        validateDeletable(permissionIds);
        permissionDataRuleMapper.delete(new LambdaQueryWrapper<PermissionDataRule>().in(PermissionDataRule::getPermissionId, permissionIds));
        return removeByIds(permissionIds);
    }

    @Override
    public List<Permission> listByUserId(String userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public List<Permission> listMenuByUserId(String userId) {
        return baseMapper.selectMenusByUserId(userId);
    }

    @Override
    public List<String> listPermissionCodesByUserId(String userId) {
        return listByUserId(userId).stream()
                .map(Permission::getPerms)
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();
    }

    @Override
    public List<PermissionTreeVO> listTree() {
        List<Permission> permissions = list(new LambdaQueryWrapper<Permission>()
                .orderByAsc(Permission::getSortNo)
                .orderByAsc(Permission::getName));
        Map<String, PermissionTreeVO> nodeMap = new LinkedHashMap<>();
        List<PermissionTreeVO> roots = new ArrayList<>();
        for (Permission permission : permissions) {
            PermissionTreeVO node = new PermissionTreeVO();
            node.setId(permission.getId());
            node.setParentId(permission.getParentId());
            node.setName(permission.getName());
            node.setUrl(permission.getUrl());
            node.setComponent(permission.getComponent());
            node.setPerms(permission.getPerms());
            node.setType(permission.getType());
            node.setIcon(permission.getIcon());
            node.setSortNo(permission.getSortNo());
            node.setHidden(permission.getHidden());
            node.setAlwaysShow(permission.getAlwaysShow());
            nodeMap.put(node.getId(), node);
        }
        for (PermissionTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId().isBlank() || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        sortNodes(roots);
        return roots;
    }

    private void sortNodes(List<PermissionTreeVO> nodes) {
        nodes.sort(Comparator.comparing(PermissionTreeVO::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(PermissionTreeVO::getName, Comparator.nullsLast(String::compareTo)));
        for (PermissionTreeVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortNodes(node.getChildren());
            }
        }
    }

    @Override
    public int getNextSortNo(String parentId) {
        Permission lastSibling = getOne(new LambdaQueryWrapper<Permission>()
                .eq(StringUtils.hasText(parentId), Permission::getParentId, parentId)
                .isNull(!StringUtils.hasText(parentId), Permission::getParentId)
                .orderByDesc(Permission::getSortNo)
                .last("limit 1"));
        if (lastSibling == null || lastSibling.getSortNo() == null) {
            return 10;
        }
        return lastSibling.getSortNo() + 10;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSort(List<PermissionSortDTO> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        for (PermissionSortDTO item : items) {
            Permission permission = new Permission();
            permission.setId(item.getId());
            permission.setParentId(item.getParentId());
            permission.setSortNo(item.getSortNo());
            updateById(permission);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionTreeSaveResultVO saveTree(List<PermissionTreeSaveDTO> tree) {
        Counter counter = new Counter();
        saveTreeNodes(tree, null, counter);
        PermissionTreeSaveResultVO result = new PermissionTreeSaveResultVO();
        result.setUpdatedCount(counter.value);
        result.setTree(listTree());
        return result;
    }

    private void saveTreeNodes(List<PermissionTreeSaveDTO> nodes, String parentId, Counter counter) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        int sortNo = 10;
        for (PermissionTreeSaveDTO node : nodes) {
            if (node == null || !StringUtils.hasText(node.getId())) {
                continue;
            }
            Permission existing = getById(node.getId());
            if (existing == null) {
                continue;
            }
            Permission permission = new Permission();
            permission.setId(node.getId());
            permission.setParentId(parentId);
            permission.setSortNo(sortNo);
            updateById(permission);
            counter.value++;
            saveTreeNodes(node.getChildren(), node.getId(), counter);
            sortNo += 10;
        }
    }

    private static final class Counter {
        private int value;
    }

    private void validateRouteUnique(Permission permission) {
        if (permission == null || !requiresUniqueRoute(permission) || !StringUtils.hasText(permission.getUrl())) {
            return;
        }
        Permission existed = getOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getUrl, permission.getUrl())
                .ne(StringUtils.hasText(permission.getId()), Permission::getId, permission.getId())
                .last("limit 1"));
        if (existed != null) {
            throw new BusinessException(400, "菜单路由已存在，请更换路由地址");
        }
    }

    private boolean requiresUniqueRoute(Permission permission) {
        Integer type = permission.getType();
        return type == null || Integer.valueOf(0).equals(type) || Integer.valueOf(1).equals(type);
    }

    private void validateDeletable(List<String> permissionIds) {
        Map<String, String> permissionNameMap = baseMapper.selectBatchIds(permissionIds).stream()
                .collect(Collectors.toMap(Permission::getId, this::getPermissionLabel, (left, right) -> left, LinkedHashMap::new));

        List<Permission> childPermissions = list(new LambdaQueryWrapper<Permission>()
                .in(Permission::getParentId, permissionIds)
                .notIn(permissionIds.size() > 1, Permission::getId, permissionIds)
                .orderByAsc(Permission::getSortNo)
                .orderByAsc(Permission::getName));
        if (!childPermissions.isEmpty()) {
            Map<String, List<String>> childDetails = new LinkedHashMap<>();
            for (Permission childPermission : childPermissions) {
                childDetails.computeIfAbsent(childPermission.getParentId(), key -> new ArrayList<>())
                        .add(getPermissionLabel(childPermission));
            }
            List<DeleteConflictItemVO> items = toGroupedItems(childDetails, permissionNameMap, "permission", "permission");
            throw new BusinessException(400,
                    "菜单删除失败，以下菜单仍有子菜单: " + formatGroupedPermissionDetail(items),
                    DeleteConflictResultVO.of("permission", List.of(
                            DeleteConflictDetailVO.of("child_permission", "子菜单", items)
                    )));
        }

        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                .in(RolePermission::getPermissionId, permissionIds));
        if (!rolePermissions.isEmpty()) {
            Map<String, String> roleNameMap = roleMapper.selectBatchIds(rolePermissions.stream()
                            .map(RolePermission::getRoleId)
                            .filter(StringUtils::hasText)
                            .distinct()
                            .toList()).stream()
                    .collect(Collectors.toMap(Role::getId, this::getRoleLabel, (left, right) -> left, LinkedHashMap::new));
            Map<String, List<String>> assignmentDetail = new LinkedHashMap<>();
            for (RolePermission rolePermission : rolePermissions) {
                assignmentDetail.computeIfAbsent(rolePermission.getPermissionId(), key -> new ArrayList<>())
                        .add(roleNameMap.getOrDefault(rolePermission.getRoleId(), rolePermission.getRoleId()));
            }
            List<DeleteConflictItemVO> items = toGroupedItems(assignmentDetail, permissionNameMap, "permission", "role");
            throw new BusinessException(400,
                    "菜单删除失败，以下菜单仍被角色授权: " + formatGroupedPermissionDetail(items),
                    DeleteConflictResultVO.of("permission", List.of(
                            DeleteConflictDetailVO.of("assigned_role", "角色授权", items)
                    )));
        }
    }

    private String formatGroupedPermissionDetail(List<DeleteConflictItemVO> items) {
        return items.stream().map(DeleteConflictItemVO::getName).collect(Collectors.joining("；"));
    }

    private List<DeleteConflictItemVO> toGroupedItems(Map<String, List<String>> groupedDetails,
                                                      Map<String, String> permissionNameMap,
                                                      String itemType,
                                                      String relatedType) {
        return groupedDetails.entrySet().stream()
                .map(entry -> DeleteConflictItemVO.of(
                        entry.getKey(),
                        permissionNameMap.getOrDefault(entry.getKey(), entry.getKey()) + "(" + String.join("、", entry.getValue()) + ")",
                        itemType,
                        buildGroupedMeta(entry.getValue(), relatedType)
                ))
                .toList();
    }

    private Map<String, Object> buildGroupedMeta(List<String> relatedNames, String relatedType) {
        if (relatedNames == null || relatedNames.isEmpty()) {
            return null;
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", "关联对象");
        meta.put("summary", "共 " + relatedNames.size() + " 个" + resolveTypeLabel(relatedType));
        meta.put("relatedType", relatedType);
        meta.put("relatedItems", relatedNames.stream()
                .map(name -> DeleteConflictItemVO.of(null, name, relatedType))
                .toList());
        return meta;
    }

    private String resolveTypeLabel(String relatedType) {
        return switch (relatedType) {
            case "role" -> "角色";
            case "permission" -> "菜单";
            case "depart" -> "部门";
            case "user" -> "用户";
            default -> "关联项";
        };
    }

    private String getPermissionLabel(Permission permission) {
        if (permission == null) {
            return "";
        }
        if (StringUtils.hasText(permission.getName())) {
            return permission.getName();
        }
        if (StringUtils.hasText(permission.getPerms())) {
            return permission.getPerms();
        }
        if (StringUtils.hasText(permission.getUrl())) {
            return permission.getUrl();
        }
        return permission.getId();
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
}
