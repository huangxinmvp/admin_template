package com.hiking.treasure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiking.treasure.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface RoleMapper extends BaseMapper<Role> {
    @Select("select r.* from sys_role r inner join sys_user_role ur on r.id=ur.role_id where ur.user_id=#{userId}")
    List<Role> selectByUserId(@Param("userId") String userId);

    @Select("select * from sys_role where role_code = #{code} limit 1")
    Role selectByCode(@Param("code") String code);
}
