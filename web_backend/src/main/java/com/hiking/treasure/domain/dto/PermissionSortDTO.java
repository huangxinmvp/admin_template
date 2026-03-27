package com.hiking.treasure.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单排序项")
public class PermissionSortDTO {
    @Schema(description = "权限ID")
    private String id;

    @Schema(description = "父级权限ID")
    private String parentId;

    @Schema(description = "排序号")
    private Integer sortNo;
}
