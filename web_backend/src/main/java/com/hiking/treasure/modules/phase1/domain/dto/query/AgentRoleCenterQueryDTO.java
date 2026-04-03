package com.hiking.treasure.modules.phase1.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS Agent 角色中心 - 查询 DTO")
public class AgentRoleCenterQueryDTO {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "角色类型")
    private String roleCategory;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "默认角色")
    private Integer defaultFlag;
}
