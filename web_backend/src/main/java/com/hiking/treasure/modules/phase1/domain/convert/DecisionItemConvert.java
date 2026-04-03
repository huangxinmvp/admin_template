package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.DecisionItemCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.DecisionItemQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.DecisionItemUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionItemVO;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DecisionItemConvert {

    DecisionItem toEntity(DecisionItemCreateDTO dto);

    DecisionItem toEntity(DecisionItemUpdateDTO dto);

    DecisionItem toEntity(DecisionItemQueryDTO dto);

    DecisionItemVO toVO(DecisionItem entity);

    List<DecisionItemVO> toVOs(List<DecisionItem> entities);
}
