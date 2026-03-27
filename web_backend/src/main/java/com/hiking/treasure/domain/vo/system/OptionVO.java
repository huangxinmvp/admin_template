package com.hiking.treasure.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "通用选项")
public class OptionVO {
    @Schema(description = "选项值")
    private String value;

    @Schema(description = "选项标签")
    private String label;

    @Schema(description = "选项编码")
    private String code;
}
