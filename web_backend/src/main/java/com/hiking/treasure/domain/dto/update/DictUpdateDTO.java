package com.hiking.treasure.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "数据字典 - 更新DTO" )
public class DictUpdateDTO {
    @Schema(description = "字典名称" )
    private String dictName;
    @Schema(description = "字典编码(唯一)" )
    private String dictCode;
    @Schema(description = "描述" )
    private String description;
    @Schema(description = "状态:1启用 0停用" )
    private Integer status;
}
