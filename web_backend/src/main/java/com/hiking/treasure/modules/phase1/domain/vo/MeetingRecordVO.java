package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "会议记录 VO")
public class MeetingRecordVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "会议标题")
    private String meetingTitle;

    @Schema(description = "原始笔记")
    private String rawNotes;

    @Schema(description = "总结")
    private String summary;

    @Schema(description = "行动项")
    private List<String> actionItems;

    @Schema(description = "开放问题")
    private List<String> openQuestions;

    @Schema(description = "决策候选")
    private List<MeetingDecisionCandidateVO> decisionCandidates;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源对象ID")
    private String sourceObjectId;

    @Schema(description = "生成人")
    private String generatedBy;

    @Schema(description = "生成时间")
    private LocalDateTime generatedAt;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
