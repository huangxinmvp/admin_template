package com.hiking.treasure.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "角色表 - 查询DTO" )
public class RoleQueryDTO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "角色名称" )
    private String roleName;
    @Schema(description = "角色编码(英文/唯一)" )
    private String roleCode;
    @Schema(description = "角色描述" )
    private String description;
    @Schema(description = "状态: 1启用 0停用" )
    private Integer status;
    @Schema(description = "删除状态: 0正常 1删除" )
    private Integer delFlag;
    @Schema(description = "备注" )
    private String remark;
    @Schema(description = "创建人" )
    private String createBy;
    @Schema(description = "创建时间" )
    private LocalDateTime createTime;
    @Schema(description = "更新人" )
    private String updateBy;
    @Schema(description = "更新时间" )
    private LocalDateTime updateTime;
    @Schema(description = "所属部门编码(可选)" )
    private String sysOrgCode;
}
