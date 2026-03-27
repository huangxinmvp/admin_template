package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "数据权限规则 - VO" )
public class PermissionDataRuleVO {
    @Schema(description = "主键ID" )
    private String id;
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
    @Schema(description = "删除状态" )
    private Integer delFlag;
    @Schema(description = "创建人" )
    private String createBy;
    @Schema(description = "创建时间" )
    private LocalDateTime createTime;
    @Schema(description = "更新人" )
    private String updateBy;
    @Schema(description = "更新时间" )
    private LocalDateTime updateTime;
}
