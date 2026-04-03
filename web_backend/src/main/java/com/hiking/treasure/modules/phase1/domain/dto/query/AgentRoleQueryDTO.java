package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS Agent 角色 - 查询DTO")
public class AgentRoleQueryDTO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色分类")
    private String roleCategory;

    @Schema(description = "状态")
    private String status;
}
