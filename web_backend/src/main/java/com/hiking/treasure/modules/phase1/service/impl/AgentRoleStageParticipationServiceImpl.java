package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.AgentRoleStageParticipation;
import com.hiking.treasure.modules.phase1.mapper.AgentRoleStageParticipationMapper;
import com.hiking.treasure.modules.phase1.service.AgentRoleStageParticipationService;
import org.springframework.stereotype.Service;

@Service
public class AgentRoleStageParticipationServiceImpl
        extends ServiceImpl<AgentRoleStageParticipationMapper, AgentRoleStageParticipation>
        implements AgentRoleStageParticipationService {
}
