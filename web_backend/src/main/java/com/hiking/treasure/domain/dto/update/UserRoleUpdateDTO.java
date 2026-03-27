package com.hiking.treasure.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户-角色 关联 - 更新DTO" )
public class UserRoleUpdateDTO {
    @Schema(description = "用户ID" )
    private String userId;
    @Schema(description = "角色ID" )
    private String roleId;
}
