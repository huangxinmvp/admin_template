package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 工具集成审计")
public class ToolIntegrationAuditVO {

    private String id;

    private String projectId;

    private String toolType;

    private String actionType;

    private String sourceObjectType;

    private String sourceObjectId;

    private String bindingId;

    private Integer previewFlag;

    private Integer confirmedFlag;

    private String auditStatus;

    private String externalObjectId;

    private String externalObjectUrl;

    private String operatorUserId;

    private String errorMessage;

    private String requestPayloadJson;

    private String responsePayloadJson;

    private LocalDateTime createTime;
}
