package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.domain.dto.command.WorkflowTemplateCenterSaveDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.WorkflowTemplateCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateCenterListVO;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplate;

public interface WorkflowTemplateService extends IService<WorkflowTemplate> {

    Page<WorkflowTemplateCenterListVO> pageTemplateCenter(
            WorkflowTemplateCenterQueryDTO dto,
            long pageNo,
            long pageSize
    );

    WorkflowTemplateCenterDetailVO getTemplateCenterDetail(String id);

    WorkflowTemplateCenterDetailVO createTemplateCenter(WorkflowTemplateCenterSaveDTO dto);

    WorkflowTemplateCenterDetailVO updateTemplateCenter(String id, WorkflowTemplateCenterSaveDTO dto);
}
