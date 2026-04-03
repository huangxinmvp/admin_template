package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.BudgetLedgerCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.BudgetLedgerQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.BudgetLedgerUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetLedgerVO;
import com.hiking.treasure.modules.phase1.entity.BudgetLedger;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BudgetLedgerConvert {

    BudgetLedger toEntity(BudgetLedgerCreateDTO dto);

    BudgetLedger toEntity(BudgetLedgerUpdateDTO dto);

    BudgetLedger toEntity(BudgetLedgerQueryDTO dto);

    BudgetLedgerVO toVO(BudgetLedger entity);

    List<BudgetLedgerVO> toVOs(List<BudgetLedger> entities);
}
