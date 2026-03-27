package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户-角色 关联 - VO" )
public class UserRoleVO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "用户ID" )
    private String userId;
    @Schema(description = "角色ID" )
    private String roleId;
    @Schema(description = "创建人" )
    private String createBy;
    @Schema(description = "创建时间" )
    private LocalDateTime createTime;
}
