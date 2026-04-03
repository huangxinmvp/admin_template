package com.hiking.treasure.modules.phase1.domain.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinearWritePreviewDTO {

    @NotBlank
    private String writeMode;

    private String teamId;

    private String title;

    private String body;

    private String targetIssueId;

    private String targetIssueIdentifier;

    private String targetIssueUrl;

    private String remark;
}
