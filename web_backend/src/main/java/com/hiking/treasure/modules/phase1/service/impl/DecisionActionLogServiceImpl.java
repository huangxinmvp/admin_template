package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.DecisionActionLog;
import com.hiking.treasure.modules.phase1.mapper.DecisionActionLogMapper;
import com.hiking.treasure.modules.phase1.service.DecisionActionLogService;
import org.springframework.stereotype.Service;

@Service
public class DecisionActionLogServiceImpl extends ServiceImpl<DecisionActionLogMapper, DecisionActionLog>
        implements DecisionActionLogService {
}
