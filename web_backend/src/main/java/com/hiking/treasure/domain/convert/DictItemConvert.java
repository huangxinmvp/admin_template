package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.DictItem;
import com.hiking.treasure.domain.dto.create.DictItemCreateDTO;
import com.hiking.treasure.domain.dto.update.DictItemUpdateDTO;
import com.hiking.treasure.domain.dto.query.DictItemQueryDTO;
import com.hiking.treasure.domain.vo.DictItemVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface DictItemConvert {
    DictItem toEntity(DictItemCreateDTO dto);

    DictItem toEntity(DictItemUpdateDTO dto);

    DictItem toEntity(DictItemQueryDTO dto);

    DictItemVO toVO(DictItem entity);

    List<DictItemVO> toVOs(List<DictItem> list);
}
