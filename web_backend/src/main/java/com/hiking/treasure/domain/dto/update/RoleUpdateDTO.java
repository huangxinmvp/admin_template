package com.hiking.treasure.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "角色表 - 更新DTO" )
public class RoleUpdateDTO {
    @Schema(description = "角色名称" )
    private String roleName;
    @Schema(description = "角色编码(英文/唯一)" )
    private String roleCode;
    @Schema(description = "角色描述" )
    private String description;
    @Schema(description = "状态: 1启用 0停用" )
    private Integer status;
    @Schema(description = "备注" )
    private String remark;
    @Schema(description = "所属部门编码(可选)" )
    private String sysOrgCode;
}
