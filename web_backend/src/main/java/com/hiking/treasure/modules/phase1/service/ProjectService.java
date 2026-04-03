package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.domain.dto.query.ProjectCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCenterListVO;
import com.hiking.treasure.modules.phase1.entity.Project;

public interface ProjectService extends IService<Project> {
    Page<ProjectCenterListVO> pageProjectCenter(ProjectCenterQueryDTO dto, long pageNo, long pageSize);

    ProjectCenterDetailVO getProjectCenterDetail(String id);
}
