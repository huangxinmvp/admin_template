package com.hiking.treasure.service.impl;

import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.mapper.UserRoleMapper;
import com.hiking.treasure.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户-角色 关联 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

}
