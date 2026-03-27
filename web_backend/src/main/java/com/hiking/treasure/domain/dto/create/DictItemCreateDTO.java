package com.hiking.treasure.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "数据字典项 - 新增DTO" )
public class DictItemCreateDTO {
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
}
