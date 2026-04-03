package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplateStage;
import com.hiking.treasure.modules.phase1.mapper.WorkflowTemplateStageMapper;
import com.hiking.treasure.modules.phase1.service.WorkflowTemplateStageService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTemplateStageServiceImpl
        extends ServiceImpl<WorkflowTemplateStageMapper, WorkflowTemplateStage>
        implements WorkflowTemplateStageService {
}
