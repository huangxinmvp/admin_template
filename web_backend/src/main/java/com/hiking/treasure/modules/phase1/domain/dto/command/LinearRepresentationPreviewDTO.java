package com.hiking.treasure.modules.phase1.domain.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinearRepresentationPreviewDTO {

    @NotBlank
    private String mode;

    private String teamId;

    private String representationTitle;

    private String representationDescription;

    private String existingIssueId;

    private String existingIssueIdentifier;

    private String existingIssueUrl;

    private String remark;
}
