package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.modules.phase1.entity.ApprovalActionLog;
import com.hiking.treasure.modules.phase1.mapper.ApprovalActionLogMapper;
import com.hiking.treasure.modules.phase1.service.ApprovalActionLogService;
import org.springframework.stereotype.Service;

@Service
public class ApprovalActionLogServiceImpl extends ServiceImpl<ApprovalActionLogMapper, ApprovalActionLog>
        implements ApprovalActionLogService {
}
