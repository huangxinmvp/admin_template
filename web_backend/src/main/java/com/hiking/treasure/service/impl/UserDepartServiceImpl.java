package com.hiking.treasure.service.impl;

import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.service.UserDepartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户-部门关系 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
public class UserDepartServiceImpl extends ServiceImpl<UserDepartMapper, UserDepart> implements UserDepartService {

}
