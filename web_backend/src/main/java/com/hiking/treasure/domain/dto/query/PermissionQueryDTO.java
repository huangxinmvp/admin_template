package com.hiking.treasure.domain.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "权限表(菜单/按钮) - 查询DTO" )
public class PermissionQueryDTO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "父ID(顶级为空)" )
    private String parentId;
    @Schema(description = "名称" )
    private String name;
    @Schema(description = "路由/接口地址" )
    private String url;
    @Schema(description = "前端组件" )
    private String component;
    @Schema(description = "权限标识(如: user:list)" )
    private String perms;
    @Schema(description = "类型:0目录 1菜单 2按钮" )
    private Integer type;
    @Schema(description = "图标" )
    private String icon;
    @Schema(description = "排序" )
    private Integer sortNo;
    @Schema(description = "是否隐藏:0否 1是" )
    private Integer hidden;
    @Schema(description = "始终显示目录" )
    private Integer alwaysShow;
    @Schema(description = "状态:1启用 0停用" )
    private Integer status;
    @Schema(description = "删除状态:0正常 1删除" )
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
}
