package com.hiking.treasure.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "权限树节点")
public class PermissionTreeVO {
    @Schema(description = "权限ID")
    private String id;

    @Schema(description = "父权限ID")
    private String parentId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "路由")
    private String url;

    @Schema(description = "组件")
    private String component;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sortNo;

    @Schema(description = "是否隐藏")
    private Integer hidden;

    @Schema(description = "始终显示")
    private Integer alwaysShow;

    @Schema(description = "子节点")
    private List<PermissionTreeVO> children = new ArrayList<>();
}
