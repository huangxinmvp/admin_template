package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.entity.ProjectToolBinding;

import java.util.List;

public interface ProjectToolBindingService extends IService<ProjectToolBinding> {

    List<ProjectToolBinding> listByProjectId(String projectId);

    ProjectToolBinding getDefaultBinding(String projectId, String toolType, String bindingType);

    ProjectToolBinding saveOrReplaceDefaultBinding(ProjectToolBinding binding);
}
