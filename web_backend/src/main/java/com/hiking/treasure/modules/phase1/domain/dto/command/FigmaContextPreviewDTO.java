package com.hiking.treasure.modules.phase1.domain.dto.command;

import lombok.Data;

@Data
public class FigmaContextPreviewDTO {

    private String figmaUrl;

    private String fileKey;

    private String nodeId;

    private String bindingName;

    private String remark;
}
