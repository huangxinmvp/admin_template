package com.hiking.treasure.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "数据权限规则 - 新增DTO" )
public class PermissionDataRuleCreateDTO {
    @Schema(description = "菜单权限ID" )
    private String permissionId;
    @Schema(description = "规则名称" )
    private String ruleName;
    @Schema(description = "限定字段" )
    private String ruleColumn;
    @Schema(description = "条件(=,!=,in,like,<=,>=,between)" )
    private String condition;
    @Schema(description = "规则值(支持变量)" )
    private String ruleValue;
    @Schema(description = "状态:1启用 0停用" )
    private Integer status;
}
