package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.domain.vo.UserVO;
import com.hiking.treasure.domain.vo.system.UserDetailVO;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.mapper.RoleMapper;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.mapper.UserMapper;
import com.hiking.treasure.mapper.UserRoleMapper;
import com.hiking.treasure.service.DepartService;
import com.hiking.treasure.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final RoleMapper roleMapper;
    private final UserDepartMapper userDepartMapper;
    private final UserRoleMapper userRoleMapper;
    private final DepartService departService;

    @Override
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username).last("limit 1"));
    }

    @Override
    public List<String> getRoleCodes(String userId) {
        return roleMapper.selectByUserId(userId).stream().map(Role::getRoleCode).toList();
    }

    @Override
    public List<String> getDepartIds(String userId) {
        return userDepartMapper.selectList(new LambdaQueryWrapper<UserDepart>()
                        .eq(UserDepart::getUserId, userId))
                .stream()
                .map(UserDepart::getDepartId)
                .toList();
    }

    @Override
    public List<UserVO> fillUserSummary(List<UserVO> users) {
        if (users == null || users.isEmpty()) {
            return users;
        }
        List<String> userIds = users.stream().map(UserVO::getId).filter(Objects::nonNull).toList();
        if (userIds.isEmpty()) {
            return users;
        }

        Map<String, List<Role>> roleMap = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                        .in(UserRole::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(UserRole::getUserId,
                        LinkedHashMap::new,
                        Collectors.mapping(UserRole::getRoleId, Collectors.toList())))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> roleMapper.selectBatchIds(entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new));

        Map<String, List<Depart>> departMap = userDepartMapper.selectList(new LambdaQueryWrapper<UserDepart>()
                        .in(UserDepart::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(UserDepart::getUserId,
                        LinkedHashMap::new,
                        Collectors.mapping(UserDepart::getDepartId, Collectors.toList())))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> departService.listByIds(entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new));

        for (UserVO user : users) {
            List<String> roleNames = roleMap.getOrDefault(user.getId(), List.of()).stream()
                    .map(Role::getRoleName)
                    .filter(Objects::nonNull)
                    .toList();
            List<String> departNames = departMap.getOrDefault(user.getId(), List.of()).stream()
                    .map(Depart::getDepartName)
                    .filter(Objects::nonNull)
                    .toList();
            user.setRoleNames(roleNames);
            user.setDepartNames(departNames);
            user.setRoleSummary(String.join(" / ", roleNames));
            user.setDepartSummary(String.join(" / ", departNames));
        }
        return users;
    }

    @Override
    public UserDetailVO getDetailAggregate(String userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        List<Role> roles = roleMapper.selectByUserId(userId);
        List<String> departIds = getDepartIds(userId);
        Map<String, Depart> departMap = departIds.isEmpty()
                ? Collections.emptyMap()
                : departService.listByIds(departIds).stream().collect(Collectors.toMap(Depart::getId, Function.identity()));

        UserDetailVO vo = new UserDetailVO();
        vo.setId(user.getId());
        vo.setTenantId(user.getTenantId());
        vo.setUsername(user.getUsername());
        vo.setRealname(user.getRealname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setLockUntil(user.getLockUntil());
        vo.setRemark(user.getRemark());
        vo.setSysOrgCode(user.getSysOrgCode());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setRoleIds(roles.stream().map(Role::getId).toList());
        vo.setRoleCodes(roles.stream().map(Role::getRoleCode).toList());
        vo.setRoleNames(roles.stream().map(Role::getRoleName).toList());
        vo.setDepartIds(departIds);
        vo.setDepartNames(departIds.stream()
                .map(departMap::get)
                .filter(java.util.Objects::nonNull)
                .map(Depart::getDepartName)
                .toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserById(String userId) {
        if (!removeById(userId)) {
            return false;
        }
        clearRelations(List.of(userId));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        boolean removed = removeByIds(userIds);
        if (!removed) {
            return false;
        }
        clearRelations(userIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserWithRelations(User user, List<String> roleIds, List<String> departIds) {
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        boolean saved = save(user);
        if (!saved) {
            throw new BusinessException(500, "用户保存失败");
        }
        saveRelations(user.getId(), roleIds, departIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserWithRelations(User user, List<String> roleIds, List<String> departIds) {
        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(500, "用户更新失败");
        }
        saveRelations(user.getId(), roleIds, departIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(String userId, String encodedPassword) {
        User existing = requireExistingUser(userId);
        User user = new User();
        user.setId(existing.getId());
        user.setPassword(encodedPassword);
        user.setPwdUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockUser(String userId, LocalDateTime lockUntil) {
        requireExistingUser(userId);
        User user = new User();
        user.setId(userId);
        user.setLockUntil(lockUntil);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlockUser(String userId) {
        requireExistingUser(userId);
        User user = new User();
        user.setId(userId);
        user.setLockUntil(null);
        return updateById(user);
    }

    private void saveRelations(String userId, List<String> roleIds, List<String> departIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        userDepartMapper.delete(new LambdaQueryWrapper<UserDepart>().eq(UserDepart::getUserId, userId));
        if (roleIds != null) {
            for (String roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        if (departIds != null) {
            for (String departId : departIds) {
                UserDepart userDepart = new UserDepart();
                userDepart.setUserId(userId);
                userDepart.setDepartId(departId);
                userDepartMapper.insert(userDepart);
            }
        }
    }

    private void clearRelations(List<String> userIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userIds));
        userDepartMapper.delete(new LambdaQueryWrapper<UserDepart>().in(UserDepart::getUserId, userIds));
    }

    private User requireExistingUser(String userId) {
        User existing = getById(userId);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return existing;
    }
}
