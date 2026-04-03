package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.domain.dto.action.ApprovalActionRequestDTO;
import com.hiking.treasure.modules.phase1.entity.ApprovalActionLog;
import com.hiking.treasure.modules.phase1.entity.ApprovalRecord;

import java.util.List;

public interface ApprovalRecordService extends IService<ApprovalRecord> {
    ApprovalRecord takeAction(String approvalRecordId, ApprovalActionRequestDTO dto);

    List<ApprovalActionLog> listActionLogs(String approvalRecordId);

    List<ApprovalRecord> listBySource(String sourceObjectType, String sourceObjectId);

    ApprovalRecord createOrOpenForDecision(String decisionItemId);

    ApprovalRecord createOrOpenForBudgetPlan(String budgetPlanId);
}
