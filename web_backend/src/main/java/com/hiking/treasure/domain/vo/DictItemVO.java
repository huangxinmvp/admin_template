package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "数据字典项 - VO" )
public class DictItemVO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "字典ID" )
    private String dictId;
    @Schema(description = "字典项文本" )
    private String itemText;
    @Schema(description = "字典项值" )
    private String itemValue;
    @Schema(description = "排序" )
    private Integer sortOrder;
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
