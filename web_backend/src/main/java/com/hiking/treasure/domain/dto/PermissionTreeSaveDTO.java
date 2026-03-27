package com.hiking.treasure.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "菜单整树保存节点")
public class PermissionTreeSaveDTO {
    @Schema(description = "权限ID")
    private String id;

    @Schema(description = "子节点")
    private List<PermissionTreeSaveDTO> children = new ArrayList<>();
}
