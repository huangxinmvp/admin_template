package com.hiking.treasure.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "系统通告 - 更新DTO" )
public class AnnouncementUpdateDTO {
    @Schema(description = "标题" )
    private String title;
    @Schema(description = "内容" )
    private String msgContent;
    @Schema(description = "开始时间" )
    private LocalDateTime startTime;
    @Schema(description = "结束时间" )
    private LocalDateTime endTime;
    @Schema(description = "发布人" )
    private String sender;
    @Schema(description = "优先级:1低 2中 3高" )
    private Integer priority;
    @Schema(description = "类别:1通知 2系统消息" )
    private Integer msgCategory;
    @Schema(description = "类型:1通知公告 2系统消息" )
    private Integer msgType;
    @Schema(description = "接收范围:ALL/TENANT/USER" )
    private String receiverScope;
    @Schema(description = "目标租户ID" )
    private String targetTenantId;
    @Schema(description = "发布状态:0未发 1已发 2撤销" )
    private Integer sendStatus;
    @Schema(description = "发布时间" )
    private LocalDateTime sendTime;
    @Schema(description = "撤销时间" )
    private LocalDateTime cancelTime;
}
