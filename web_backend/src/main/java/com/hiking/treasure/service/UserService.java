package com.hiking.treasure.service;

import com.hiking.treasure.domain.vo.system.UserDetailVO;
import com.hiking.treasure.domain.vo.UserVO;
import com.hiking.treasure.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface UserService extends IService<User> {
    User getByUsername(String username);
    List<String> getRoleCodes(String userId);
    List<String> getDepartIds(String userId);
    List<UserVO> fillUserSummary(List<UserVO> users);
    UserDetailVO getDetailAggregate(String userId);
    boolean deleteUserById(String userId);
    boolean deleteUsersByIds(List<String> userIds);
    boolean saveUserWithRelations(User user, List<String> roleIds, List<String> departIds);
    boolean updateUserWithRelations(User user, List<String> roleIds, List<String> departIds);
    boolean resetPassword(String userId, String encodedPassword);
    boolean lockUser(String userId, LocalDateTime lockUntil);
    boolean unlockUser(String userId);
}
