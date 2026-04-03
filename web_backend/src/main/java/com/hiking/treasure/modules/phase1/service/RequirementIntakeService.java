package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;

public interface RequirementIntakeService extends IService<RequirementIntake> {
    RequirementIntake getByProjectId(String projectId);

    RequirementIntake saveProjectIntake(String projectId, RequirementIntake intake);
}
