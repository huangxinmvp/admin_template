package com.hiking.treasure.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "部门树节点")
public class DepartTreeVO {
    @Schema(description = "部门ID")
    private String id;

    @Schema(description = "父部门ID")
    private String parentId;

    @Schema(description = "部门名称")
    private String departName;

    @Schema(description = "机构编码")
    private String orgCode;

    @Schema(description = "排序")
    private Integer departOrder;

    @Schema(description = "机构类别")
    private Integer orgCategory;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "子节点")
    private List<DepartTreeVO> children = new ArrayList<>();
}
