package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "文件中心统计信息")
public class FileCenterStatsVO {
    @Schema(description = "文件总数")
    private Long totalCount;

    @Schema(description = "文件总大小(字节)")
    private Long totalSize;

    @Schema(description = "今日上传文件数")
    private Long todayUploadCount;

    @Schema(description = "今日上传文件大小(字节)")
    private Long todayUploadSize;

    @Schema(description = "业务类型数量")
    private Integer bizTypeCount;

    @Schema(description = "存储提供方数量")
    private Integer providerCount;

    @Schema(description = "默认存储提供方")
    private String storageProvider;

    @Schema(description = "默认业务类型")
    private String defaultBizType;

    @Schema(description = "文件大小上限(MB)")
    private Integer maxSizeMb;

    @Schema(description = "允许上传的扩展名")
    private List<String> allowedTypes;

    @Schema(description = "业务类型统计")
    private List<FileCenterMetricVO> bizTypeMetrics;

    @Schema(description = "存储提供方统计")
    private List<FileCenterMetricVO> providerMetrics;
}
