package com.hiking.treasure.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "部门 - 新增DTO" )
public class DepartCreateDTO {
    @Schema(description = "父级部门ID" )
    private String parentId;
    @Schema(description = "部门名称" )
    private String departName;
    @Schema(description = "机构编码(树/数据权限编码)" )
    private String orgCode;
    @Schema(description = "排序" )
    private Integer departOrder;
    @Schema(description = "机构类别:1公司 2部门 3岗位" )
    private Integer orgCategory;
    @Schema(description = "状态:1启用 0停用" )
    private Integer status;
    @Schema(description = "备注" )
    private String remark;
}
