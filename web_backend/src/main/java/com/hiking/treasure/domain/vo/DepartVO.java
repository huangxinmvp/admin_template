package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "部门 - VO" )
public class DepartVO {
    @Schema(description = "主键ID" )
    private String id;
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
