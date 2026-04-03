package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.domain.dto.action.DecisionActionRequestDTO;
import com.hiking.treasure.modules.phase1.entity.DecisionActionLog;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;

import java.util.List;

public interface DecisionItemService extends IService<DecisionItem> {
    DecisionItem takeAction(String decisionItemId, DecisionActionRequestDTO dto);

    List<DecisionActionLog> listActionLogs(String decisionItemId);

    DecisionItem promoteFromClarification(String clarificationItemId);
}
