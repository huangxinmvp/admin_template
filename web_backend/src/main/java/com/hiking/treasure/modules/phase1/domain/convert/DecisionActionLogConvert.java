package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.vo.DecisionActionLogVO;
import com.hiking.treasure.modules.phase1.entity.DecisionActionLog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DecisionActionLogConvert {

    DecisionActionLogVO toVO(DecisionActionLog entity);

    List<DecisionActionLogVO> toVOs(List<DecisionActionLog> entities);
}
