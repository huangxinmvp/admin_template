package com.hiking.treasure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiking.treasure.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    @Select("""
            select *
            from sys_system_config
            where del_flag = 0
              and status = 1
              and (tenant_id is null or tenant_id = '')
            order by group_sort asc, sort_no asc, config_key asc
            """)
    List<SystemConfig> selectActivePlatformConfigs();

    @Select("""
            select *
            from sys_system_config
            where del_flag = 0
              and status = 1
              and config_key = #{configKey}
              and (tenant_id is null or tenant_id = '')
            limit 1
            """)
    SystemConfig selectActiveByKey(@Param("configKey") String configKey);
}
