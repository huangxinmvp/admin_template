package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.ProjectToolBinding;
import com.hiking.treasure.modules.phase1.mapper.ProjectToolBindingMapper;
import com.hiking.treasure.modules.phase1.service.ProjectToolBindingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProjectToolBindingServiceImpl extends ServiceImpl<ProjectToolBindingMapper, ProjectToolBinding>
        implements ProjectToolBindingService {

    @Override
    public List<ProjectToolBinding> listByProjectId(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<ProjectToolBinding>()
                .eq(ProjectToolBinding::getProjectId, projectId)
                .orderByDesc(ProjectToolBinding::getDefaultFlag)
                .orderByDesc(ProjectToolBinding::getUpdateTime)
                .orderByDesc(ProjectToolBinding::getCreateTime));
    }

    @Override
    public ProjectToolBinding getDefaultBinding(String projectId, String toolType, String bindingType) {
        if (projectId == null || projectId.isBlank()) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<ProjectToolBinding>()
                .eq(ProjectToolBinding::getProjectId, projectId)
                .eq(ProjectToolBinding::getToolType, toolType)
                .eq(bindingType != null && !bindingType.isBlank(), ProjectToolBinding::getBindingType, bindingType)
                .eq(ProjectToolBinding::getDefaultFlag, 1)
                .last("limit 1"));
    }

    @Override
    public ProjectToolBinding saveOrReplaceDefaultBinding(ProjectToolBinding binding) {
        if (binding == null) {
            return null;
        }
        if (Objects.equals(binding.getDefaultFlag(), 1)) {
            update(new LambdaUpdateWrapper<ProjectToolBinding>()
                    .eq(ProjectToolBinding::getProjectId, binding.getProjectId())
                    .eq(ProjectToolBinding::getToolType, binding.getToolType())
                    .eq(ProjectToolBinding::getBindingType, binding.getBindingType())
                    .set(ProjectToolBinding::getDefaultFlag, 0));
        }
        save(binding);
        return binding;
    }
}
