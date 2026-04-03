package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS Linear 写入预览")
public class LinearWritePreviewVO {

    private String writeMode;

    private String toolType;

    private String sourceObjectType;

    private String sourceObjectId;

    private String projectId;

    private String teamId;

    private String targetIssueId;

    private String targetIssueIdentifier;

    private String targetIssueUrl;

    private String title;

    private String body;

    private String operationSummary;
}
