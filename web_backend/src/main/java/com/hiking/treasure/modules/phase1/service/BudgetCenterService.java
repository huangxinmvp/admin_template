package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.modules.phase1.domain.dto.query.BudgetCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetCenterListVO;

public interface BudgetCenterService {

    Page<BudgetCenterListVO> pageBudgetCenter(BudgetCenterQueryDTO dto, long pageNo, long pageSize);

    BudgetCenterDetailVO getProjectBudgetDetail(String projectId);
}
