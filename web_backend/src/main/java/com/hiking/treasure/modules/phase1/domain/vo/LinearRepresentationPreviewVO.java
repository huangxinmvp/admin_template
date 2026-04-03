package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS Linear 项目映射预览")
public class LinearRepresentationPreviewVO {

    private String mode;

    private String projectId;

    private String teamId;

    private String representationTitle;

    private String representationDescription;

    private String existingIssueId;

    private String existingIssueIdentifier;

    private String existingIssueUrl;

    private String operationSummary;
}
