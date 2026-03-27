package com.hiking.treasure.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "角色-权限 关联 - 更新DTO" )
public class RolePermissionUpdateDTO {
    @Schema(description = "角色ID" )
    private String roleId;
    @Schema(description = "权限ID" )
    private String permissionId;
    @Schema(description = "数据规则ID集合(可选)" )
    private String dataRuleIds;
}
