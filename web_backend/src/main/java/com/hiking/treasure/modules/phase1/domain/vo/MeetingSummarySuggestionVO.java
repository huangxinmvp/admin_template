package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "会议总结建议 VO")
public class MeetingSummarySuggestionVO {

    @Schema(description = "会议标题")
    private String meetingTitle;

    @Schema(description = "总结")
    private String summary;

    @Schema(description = "行动项")
    private List<String> actionItems;

    @Schema(description = "开放问题")
    private List<String> openQuestions;

    @Schema(description = "决策候选")
    private List<MeetingDecisionCandidateVO> decisionCandidates;
}
