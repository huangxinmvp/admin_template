package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.AgentRoleAllowedAction;
import com.hiking.treasure.modules.phase1.mapper.AgentRoleAllowedActionMapper;
import com.hiking.treasure.modules.phase1.service.AgentRoleAllowedActionService;
import org.springframework.stereotype.Service;

@Service
public class AgentRoleAllowedActionServiceImpl
        extends ServiceImpl<AgentRoleAllowedActionMapper, AgentRoleAllowedAction>
        implements AgentRoleAllowedActionService {
}
