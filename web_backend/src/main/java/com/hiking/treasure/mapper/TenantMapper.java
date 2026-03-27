package com.hiking.treasure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiking.treasure.entity.Tenant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TenantMapper extends BaseMapper<Tenant> {

    @Select("select * from sys_tenant where tenant_code = #{tenantCode} and del_flag = 0 limit 1")
    Tenant selectByCode(@Param("tenantCode") String tenantCode);
}
