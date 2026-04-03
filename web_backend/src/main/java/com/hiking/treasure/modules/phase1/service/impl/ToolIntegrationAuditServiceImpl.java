package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.ToolIntegrationAudit;
import com.hiking.treasure.modules.phase1.mapper.ToolIntegrationAuditMapper;
import com.hiking.treasure.modules.phase1.service.ToolIntegrationAuditService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolIntegrationAuditServiceImpl extends ServiceImpl<ToolIntegrationAuditMapper, ToolIntegrationAudit>
        implements ToolIntegrationAuditService {

    @Override
    public List<ToolIntegrationAudit> listByProjectId(String projectId, int limit) {
        if (projectId == null || projectId.isBlank()) {
            return List.of();
        }
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 20);
        return list(new LambdaQueryWrapper<ToolIntegrationAudit>()
                .eq(ToolIntegrationAudit::getProjectId, projectId)
                .orderByDesc(ToolIntegrationAudit::getCreateTime)
                .last("limit " + safeLimit));
    }
}
