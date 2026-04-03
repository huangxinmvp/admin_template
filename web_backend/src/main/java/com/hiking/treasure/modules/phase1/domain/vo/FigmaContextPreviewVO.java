package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS Figma 上下文预览")
public class FigmaContextPreviewVO {

    private String projectId;

    private String bindingType;

    private String bindingName;

    private String figmaUrl;

    private String fileKey;

    private String fileName;

    private String nodeId;

    private String nodeName;

    private String externalUrl;

    private String lastModifiedAt;

    private String operationSummary;

    private String metadataJson;
}
