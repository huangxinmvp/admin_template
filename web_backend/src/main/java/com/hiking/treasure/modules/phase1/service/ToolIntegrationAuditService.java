package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.entity.ToolIntegrationAudit;

import java.util.List;

public interface ToolIntegrationAuditService extends IService<ToolIntegrationAudit> {

    List<ToolIntegrationAudit> listByProjectId(String projectId, int limit);
}
