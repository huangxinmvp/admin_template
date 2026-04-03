package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目中心 - 最近活动")
public class ProjectActivityVO {

    @Schema(description = "活动类型")
    private String activityType;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "活动说明")
    private String description;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "外部对象URL")
    private String externalUrl;

    @Schema(description = "发生时间")
    private LocalDateTime occurredAt;
}
