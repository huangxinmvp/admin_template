package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.domain.vo.system.DepartTreeVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictDetailVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictItemVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.domain.vo.system.OptionVO;
import com.hiking.treasure.mapper.DepartMapper;
import com.hiking.treasure.mapper.UserMapper;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.service.DepartService;
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

/**
 * <p>
 * 部门 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
@RequiredArgsConstructor
public class DepartServiceImpl extends ServiceImpl<DepartMapper, Depart> implements DepartService {

    private final UserDepartMapper userDepartMapper;
    private final UserMapper userMapper;

    @Override
    public List<DepartTreeVO> listTree() {
        List<Depart> departs = list(new LambdaQueryWrapper<Depart>()
                .orderByAsc(Depart::getDepartOrder)
                .orderByAsc(Depart::getDepartName));
        Map<String, DepartTreeVO> nodeMap = new LinkedHashMap<>();
        List<DepartTreeVO> roots = new ArrayList<>();
        for (Depart depart : departs) {
            DepartTreeVO node = new DepartTreeVO();
            node.setId(depart.getId());
            node.setParentId(depart.getParentId());
            node.setDepartName(depart.getDepartName());
            node.setOrgCode(depart.getOrgCode());
            node.setDepartOrder(depart.getDepartOrder());
            node.setOrgCategory(depart.getOrgCategory());
            node.setStatus(depart.getStatus());
            nodeMap.put(node.getId(), node);
        }
        for (DepartTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId().isBlank() || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        sortNodes(roots);
        return roots;
    }

    @Override
    public List<OptionVO> listOptions() {
        return list(new LambdaQueryWrapper<Depart>()
                .eq(Depart::getStatus, 1)
                .orderByAsc(Depart::getDepartOrder)
                .orderByAsc(Depart::getDepartName))
                .stream()
                .map(depart -> {
                    OptionVO option = new OptionVO();
                    option.setValue(depart.getId());
                    option.setLabel(depart.getDepartName());
                    option.setCode(depart.getOrgCode());
                    return option;
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDepartById(String departId) {
        validateDeletable(List.of(departId));
        return removeById(departId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDepartsByIds(List<String> departIds) {
        if (departIds == null || departIds.isEmpty()) {
            return true;
        }
        validateDeletable(departIds);
        return removeByIds(departIds);
    }

    private void sortNodes(List<DepartTreeVO> nodes) {
        nodes.sort(Comparator.comparing(DepartTreeVO::getDepartOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DepartTreeVO::getDepartName, Comparator.nullsLast(String::compareTo)));
        for (DepartTreeVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortNodes(node.getChildren());
            }
        }
    }

    private void validateDeletable(List<String> departIds) {
        Map<String, String> departNameMap = baseMapper.selectBatchIds(departIds).stream()
                .collect(java.util.stream.Collectors.toMap(Depart::getId, this::getDepartLabel, (left, right) -> left, LinkedHashMap::new));

        List<Depart> childDeparts = list(new LambdaQueryWrapper<Depart>()
                .in(Depart::getParentId, departIds)
                .notIn(departIds.size() > 1, Depart::getId, departIds));
        if (!childDeparts.isEmpty()) {
            Map<String, List<String>> childDetail = new LinkedHashMap<>();
            for (Depart childDepart : childDeparts) {
                childDetail.computeIfAbsent(childDepart.getParentId(), key -> new ArrayList<>())
                        .add(getDepartLabel(childDepart));
            }
            List<DeleteConflictItemVO> items = childDetail.entrySet().stream()
                    .map(entry -> DeleteConflictItemVO.of(
                            entry.getKey(),
                            departNameMap.getOrDefault(entry.getKey(), entry.getKey()) + "(" + String.join("、", entry.getValue()) + ")",
                            "depart",
                            buildRelatedMeta(entry.getValue(), "depart")
                    ))
                    .toList();
            throw new BusinessException(400,
                    "部门删除失败，以下部门仍有子部门: " + items.stream().map(DeleteConflictItemVO::getName).collect(java.util.stream.Collectors.joining("；")),
                    DeleteConflictResultVO.of("depart", List.of(
                            DeleteConflictDetailVO.of("child_depart", "子部门", items)
                    )));
        }

        List<UserDepart> userDeparts = userDepartMapper.selectList(new LambdaQueryWrapper<UserDepart>().in(UserDepart::getDepartId, departIds));
        if (!userDeparts.isEmpty()) {
            Map<String, String> userNameMap = userMapper.selectBatchIds(userDeparts.stream()
                            .map(UserDepart::getUserId)
                            .filter(StringUtils::hasText)
                            .distinct()
                            .toList()).stream()
                    .collect(java.util.stream.Collectors.toMap(User::getId, this::getUserLabel, (left, right) -> left, LinkedHashMap::new));
            Map<String, List<String>> assignmentDetail = new LinkedHashMap<>();
            for (UserDepart userDepart : userDeparts) {
                assignmentDetail.computeIfAbsent(userDepart.getDepartId(), key -> new ArrayList<>())
                        .add(userNameMap.getOrDefault(userDepart.getUserId(), userDepart.getUserId()));
            }
            List<DeleteConflictItemVO> items = assignmentDetail.entrySet().stream()
                    .map(entry -> DeleteConflictItemVO.of(
                            entry.getKey(),
                            departNameMap.getOrDefault(entry.getKey(), entry.getKey()) + "(" + String.join("、", entry.getValue()) + ")",
                            "depart",
                            buildRelatedMeta(entry.getValue(), "user")
                    ))
                    .toList();
            throw new BusinessException(400,
                    "部门删除失败，以下部门仍有关联用户: " + items.stream().map(DeleteConflictItemVO::getName).collect(java.util.stream.Collectors.joining("；")),
                    DeleteConflictResultVO.of("depart", List.of(
                            DeleteConflictDetailVO.of("assigned_user", "关联用户", items)
                    )));
        }
    }

    private String getDepartLabel(Depart depart) {
        if (depart == null) {
            return "";
        }
        if (StringUtils.hasText(depart.getDepartName())) {
            return depart.getDepartName();
        }
        if (StringUtils.hasText(depart.getOrgCode())) {
            return depart.getOrgCode();
        }
        return depart.getId();
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
        meta.put("title", "关联" + ("depart".equals(relatedType) ? "子部门" : "用户"));
        meta.put("summary", "共 " + relatedNames.size() + " 个" + ("depart".equals(relatedType) ? "子部门" : "用户"));
        meta.put("relatedType", relatedType);
        meta.put("relatedItems", relatedNames.stream()
                .map(name -> DeleteConflictItemVO.of(null, name, relatedType))
                .toList());
        return meta;
    }
}
