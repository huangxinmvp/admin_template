package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "通告-用户关系 - VO" )
public class AnnouncementSendVO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "通告ID" )
    private String anntId;
    @Schema(description = "用户ID" )
    private String userId;
    @Schema(description = "是否已读:0未读 1已读" )
    private Integer readFlag;
    @Schema(description = "阅读时间" )
    private LocalDateTime readTime;
}
