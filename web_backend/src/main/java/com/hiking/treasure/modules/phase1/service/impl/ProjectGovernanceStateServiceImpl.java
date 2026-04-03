package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.ProjectGovernanceState;
import com.hiking.treasure.modules.phase1.mapper.ProjectGovernanceStateMapper;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceStateService;
import org.springframework.stereotype.Service;

@Service
public class ProjectGovernanceStateServiceImpl
        extends ServiceImpl<ProjectGovernanceStateMapper, ProjectGovernanceState>
        implements ProjectGovernanceStateService {
}
