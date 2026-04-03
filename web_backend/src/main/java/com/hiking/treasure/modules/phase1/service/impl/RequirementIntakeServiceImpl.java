package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import com.hiking.treasure.modules.phase1.mapper.ProjectMapper;
import com.hiking.treasure.modules.phase1.mapper.RequirementIntakeMapper;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.RequirementIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequirementIntakeServiceImpl extends ServiceImpl<RequirementIntakeMapper, RequirementIntake>
        implements RequirementIntakeService {

    private final ProjectMapper projectMapper;
    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Override
    public RequirementIntake getByProjectId(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<RequirementIntake>()
                .eq(RequirementIntake::getProjectId, projectId)
                .last("limit 1"));
    }

    @Override
    public RequirementIntake saveProjectIntake(String projectId, RequirementIntake intake) {
        if (projectId == null || projectId.isBlank()) {
            throw new BusinessException(400, "项目ID不能为空");
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }

        RequirementIntake existing = getByProjectId(projectId);
        intake.setProjectId(projectId);
        if (existing != null) {
            intake.setId(existing.getId());
            updateById(intake);
        } else {
            save(intake);
        }

        Project projectToUpdate = new Project();
        projectToUpdate.setId(projectId);
        projectToUpdate.setProjectName(firstNonBlank(intake.getProjectName(), project.getProjectName()));
        projectToUpdate.setProjectType(firstNonBlank(intake.getProjectType(), project.getProjectType()));
        projectToUpdate.setIntakeSummary(buildProjectIntakeSummary(intake));
        projectMapper.updateById(projectToUpdate);
        projectGovernanceLinkageService.recomputeProject(projectId);
        return getByProjectId(projectId);
    }

    private String buildProjectIntakeSummary(RequirementIntake intake) {
        List<String> parts = new ArrayList<>();
        addIfPresent(parts, intake.getBusinessGoal());
        addIfPresent(parts, intake.getFeatureSummary());
        addIfPresent(parts, intake.getTimelineExpectation());
        addIfPresent(parts, intake.getBudgetRange());
        return parts.isEmpty() ? null : String.join(" | ", parts);
    }

    private void addIfPresent(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value.trim());
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
