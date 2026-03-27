package com.hiking.treasure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiking.treasure.domain.vo.system.UserSessionCountVO;
import com.hiking.treasure.entity.UserSession;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserSessionMapper extends BaseMapper<UserSession> {

    @Select("""
            select *
            from sys_user_session
            where del_flag = 0
              and user_id = #{userId}
            order by ifnull(login_time, create_time) desc, id desc
            """)
    List<UserSession> selectByUserId(@Param("userId") String userId);

    @Select("""
            select *
            from sys_user_session
            where del_flag = 0
              and status = 1
              and id = #{sessionId}
              and (refresh_expires_at is null or refresh_expires_at > now())
            limit 1
            """)
    UserSession selectActiveById(@Param("sessionId") String sessionId);

    @Select({
            "<script>",
            "select user_id as userId, count(1) as activeCount",
            "from sys_user_session",
            "where del_flag = 0",
            "  and status = 1",
            "  and (refresh_expires_at is null or refresh_expires_at > now())",
            "  and user_id in",
            "  <foreach collection='userIds' item='userId' open='(' separator=',' close=')'>",
            "    #{userId}",
            "  </foreach>",
            "group by user_id",
            "</script>"
    })
    List<UserSessionCountVO> countActiveByUserIds(@Param("userIds") List<String> userIds);
}
