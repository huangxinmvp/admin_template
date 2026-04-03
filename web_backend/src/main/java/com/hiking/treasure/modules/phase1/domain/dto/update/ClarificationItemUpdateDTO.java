package com.hiking.treasure.modules.phase1.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 澄清项 - 更新DTO")
public class ClarificationItemUpdateDTO {

    @Schema(description = "项目ID")
    private String projectId;

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

    @Schema(description = "用户回复")
    private String userResponse;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否为生成项")
    private Integer generatedFlag;

    @Schema(description = "备注")
    private String remark;
}
