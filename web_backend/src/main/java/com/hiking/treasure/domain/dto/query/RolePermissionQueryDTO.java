package com.hiking.treasure.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "角色-权限 关联 - 查询DTO" )
public class RolePermissionQueryDTO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "角色ID" )
    private String roleId;
    @Schema(description = "权限ID" )
    private String permissionId;
    @Schema(description = "数据规则ID集合(可选)" )
    private String dataRuleIds;
    @Schema(description = "创建人" )
    private String createBy;
    @Schema(description = "创建时间" )
    private LocalDateTime createTime;
}
