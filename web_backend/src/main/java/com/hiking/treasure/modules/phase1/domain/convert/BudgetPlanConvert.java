package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.BudgetPlanCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.BudgetPlanQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.BudgetPlanUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetPlanVO;
import com.hiking.treasure.modules.phase1.entity.BudgetPlan;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BudgetPlanConvert {

    BudgetPlan toEntity(BudgetPlanCreateDTO dto);

    BudgetPlan toEntity(BudgetPlanUpdateDTO dto);

    BudgetPlan toEntity(BudgetPlanQueryDTO dto);

    BudgetPlanVO toVO(BudgetPlan entity);

    List<BudgetPlanVO> toVOs(List<BudgetPlan> entities);
}
