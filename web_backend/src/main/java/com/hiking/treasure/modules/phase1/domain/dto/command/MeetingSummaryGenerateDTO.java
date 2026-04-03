package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "生成会议总结 DTO")
public class MeetingSummaryGenerateDTO {

    @Schema(description = "会议标题")
    private String meetingTitle;

    @Schema(description = "原始笔记")
    private String rawNotes;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源对象ID")
    private String sourceObjectId;
}
