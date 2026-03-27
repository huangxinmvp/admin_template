package com.hiking.treasure.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "角色树节点")
public class RoleTreeVO {
    @Schema(description = "角色ID")
    private String id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "子节点")
    private List<RoleTreeVO> children = new ArrayList<>();
}
