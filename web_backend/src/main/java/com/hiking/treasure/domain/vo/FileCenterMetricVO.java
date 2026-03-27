package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件中心统计项")
public class FileCenterMetricVO {
    @Schema(description = "标签")
    private String label;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "累计大小(字节)")
    private Long totalSize;
}
