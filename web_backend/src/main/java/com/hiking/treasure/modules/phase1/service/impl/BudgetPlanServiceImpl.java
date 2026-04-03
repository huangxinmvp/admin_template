package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.BudgetPlan;
import com.hiking.treasure.modules.phase1.mapper.BudgetPlanMapper;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.BudgetPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BudgetPlanServiceImpl extends ServiceImpl<BudgetPlanMapper, BudgetPlan> implements BudgetPlanService {

    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Override
    public boolean save(BudgetPlan entity) {
        boolean saved = super.save(entity);
        if (saved) {
            projectGovernanceLinkageService.recomputeProject(entity == null ? null : entity.getProjectId());
        }
        return saved;
    }

    @Override
    public boolean updateById(BudgetPlan entity) {
        String projectId = resolveProjectId(entity == null ? null : entity.getId(), entity == null ? null : entity.getProjectId());
        boolean updated = super.updateById(entity);
        if (updated) {
            projectGovernanceLinkageService.recomputeProject(projectId);
        }
        return updated;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        String projectId = resolveProjectId(Objects.toString(id, null), null);
        boolean removed = super.removeById(id);
        if (removed) {
            projectGovernanceLinkageService.recomputeProject(projectId);
        }
        return removed;
    }

    @Override
    public boolean removeByIds(Collection<?> list) {
        List<java.io.Serializable> ids = normalizeIds(list);
        List<String> projectIds = listByIds(ids).stream()
                .map(BudgetPlan::getProjectId)
                .filter(Objects::nonNull)
                .toList();
        boolean removed = super.removeByIds(ids);
        if (removed) {
            projectGovernanceLinkageService.recomputeProjects(projectIds);
        }
        return removed;
    }

    private String resolveProjectId(String id, String fallbackProjectId) {
        if (fallbackProjectId != null && !fallbackProjectId.isBlank()) {
            return fallbackProjectId;
        }
        if (id == null || id.isBlank()) {
            return null;
        }
        return lambdaQuery()
                .select(BudgetPlan::getProjectId)
                .eq(BudgetPlan::getId, id)
                .last("LIMIT 1")
                .oneOpt()
                .map(BudgetPlan::getProjectId)
                .orElse(null);
    }

    private List<java.io.Serializable> normalizeIds(Collection<?> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> (java.io.Serializable) item)
                .toList();
    }
}
