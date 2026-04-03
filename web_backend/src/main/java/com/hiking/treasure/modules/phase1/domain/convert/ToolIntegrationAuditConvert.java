package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.vo.ToolIntegrationAuditVO;
import com.hiking.treasure.modules.phase1.entity.ToolIntegrationAudit;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolIntegrationAuditConvert {

    public ToolIntegrationAuditVO toVO(ToolIntegrationAudit entity) {
        if (entity == null) {
            return null;
        }
        ToolIntegrationAuditVO vo = new ToolIntegrationAuditVO();
        vo.setId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setToolType(entity.getToolType());
        vo.setActionType(entity.getActionType());
        vo.setSourceObjectType(entity.getSourceObjectType());
        vo.setSourceObjectId(entity.getSourceObjectId());
        vo.setBindingId(entity.getBindingId());
        vo.setPreviewFlag(entity.getPreviewFlag());
        vo.setConfirmedFlag(entity.getConfirmedFlag());
        vo.setAuditStatus(entity.getAuditStatus());
        vo.setExternalObjectId(entity.getExternalObjectId());
        vo.setExternalObjectUrl(entity.getExternalObjectUrl());
        vo.setOperatorUserId(entity.getOperatorUserId());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setRequestPayloadJson(entity.getRequestPayloadJson());
        vo.setResponsePayloadJson(entity.getResponsePayloadJson());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    public List<ToolIntegrationAuditVO> toVOs(List<ToolIntegrationAudit> entities) {
        return entities == null ? List.of() : entities.stream().map(this::toVO).toList();
    }
}
