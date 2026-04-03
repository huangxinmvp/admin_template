package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 决策动作日志 - VO")
public class DecisionActionLogVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "决策事项ID")
    private String decisionItemId;

    @Schema(description = "动作类型")
    private String actionType;

    @Schema(description = "动作说明")
    private String actionComment;

    @Schema(description = "原状态")
    private String previousStatus;

    @Schema(description = "目标状态")
    private String nextStatus;

    @Schema(description = "操作人用户ID")
    private String operatorUserId;

    @Schema(description = "操作时间")
    private LocalDateTime operatedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
