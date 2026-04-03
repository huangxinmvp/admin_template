package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "会议总结中的决策候选")
public class MeetingDecisionCandidateVO {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "决策类型")
    private String decisionType;

    @Schema(description = "是否阻塞")
    private Integer blockerFlag;

    @Schema(description = "推荐选项")
    private String recommendedOption;
}
