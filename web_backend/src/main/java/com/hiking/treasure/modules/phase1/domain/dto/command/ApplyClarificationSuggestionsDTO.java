package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "应用澄清建议 DTO")
public class ApplyClarificationSuggestionsDTO {

    @Schema(description = "选中的建议")
    private List<SuggestionItem> suggestions;

    @Schema(description = "来源上下文")
    private String sourceContext;

    @Data
    @Schema(description = "澄清建议条目")
    public static class SuggestionItem {
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
}
