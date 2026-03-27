package com.hiking.treasure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiking.treasure.entity.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 权限表(菜单/按钮) Mapper 接口
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("""
            select distinct p.*
            from sys_permission p
            inner join sys_role_permission rp on rp.permission_id = p.id
            inner join sys_user_role ur on ur.role_id = rp.role_id
            inner join sys_role r on r.id = ur.role_id
            where ur.user_id = #{userId}
              and p.del_flag = 0
              and p.status = 1
              and r.del_flag = 0
              and r.status = 1
            order by ifnull(p.sort_no, 0), p.create_time, p.id
            """)
    List<Permission> selectByUserId(@Param("userId") String userId);

    @Select("""
            select distinct p.*
            from sys_permission p
            inner join sys_role_permission rp on rp.permission_id = p.id
            inner join sys_user_role ur on ur.role_id = rp.role_id
            inner join sys_role r on r.id = ur.role_id
            where ur.user_id = #{userId}
              and p.del_flag = 0
              and p.status = 1
              and r.del_flag = 0
              and r.status = 1
              and p.type in (0, 1)
            order by ifnull(p.sort_no, 0), p.create_time, p.id
            """)
    List<Permission> selectMenusByUserId(@Param("userId") String userId);

}
