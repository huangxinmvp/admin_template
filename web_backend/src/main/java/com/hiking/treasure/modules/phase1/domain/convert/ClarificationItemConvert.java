package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.ClarificationItemCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ClarificationItemQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.ClarificationItemUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ClarificationItemVO;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClarificationItemConvert {

    ClarificationItem toEntity(ClarificationItemCreateDTO dto);

    ClarificationItem toEntity(ClarificationItemUpdateDTO dto);

    ClarificationItem toEntity(ClarificationItemQueryDTO dto);

    ClarificationItemVO toVO(ClarificationItem entity);

    List<ClarificationItemVO> toVOs(List<ClarificationItem> entities);
}
