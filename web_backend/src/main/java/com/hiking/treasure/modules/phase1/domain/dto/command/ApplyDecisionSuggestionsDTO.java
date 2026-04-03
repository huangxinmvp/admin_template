package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "应用决策提升建议 DTO")
public class ApplyDecisionSuggestionsDTO {

    @Schema(description = "选中的建议")
    private List<SuggestionItem> suggestions;

    @Data
    @Schema(description = "决策提升建议条目")
    public static class SuggestionItem {
        @Schema(description = "澄清项ID")
        private String clarificationId;
        @Schema(description = "建议标题")
        private String suggestedTitle;
        @Schema(description = "决策类型")
        private String type;
        @Schema(description = "影响摘要")
        private String impactSummary;
        @Schema(description = "建议选项")
        private String suggestedOptions;
        @Schema(description = "推荐选项")
        private String recommendedOption;
        @Schema(description = "是否阻塞")
        private Integer blockerFlag;
        @Schema(description = "原因")
        private String reason;
    }
}
