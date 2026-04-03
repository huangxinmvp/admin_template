package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "澄清建议 VO")
public class ClarificationSuggestionVO {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "问题")
    private String question;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "严重等级")
    private String severity;

    @Schema(description = "建议选项")
    private String suggestedOptions;

    @Schema(description = "是否阻塞")
    private Integer blockerFlag;

    @Schema(description = "原因")
    private String reason;

    @Schema(description = "治理解释")
    private String governanceReason;

    @Schema(description = "建议跟进模块")
    private String followUpModule;

    @Schema(description = "是否建议提升为决策")
    private Integer escalationRecommended;
}
