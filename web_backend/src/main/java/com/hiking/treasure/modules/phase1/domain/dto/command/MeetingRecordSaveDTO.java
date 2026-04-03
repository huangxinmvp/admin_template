package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "保存会议记录 DTO")
public class MeetingRecordSaveDTO {

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
    private List<MeetingDecisionCandidateInputDTO> decisionCandidates;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源对象ID")
    private String sourceObjectId;

    @Schema(description = "备注")
    private String remark;

    @Data
    @Schema(description = "会议决策候选输入")
    public static class MeetingDecisionCandidateInputDTO {
        @Schema(description = "标题")
        private String title;
        @Schema(description = "决策类型")
        private String decisionType;
        @Schema(description = "是否阻塞")
        private Integer blockerFlag;
        @Schema(description = "推荐选项")
        private String recommendedOption;
    }
}
