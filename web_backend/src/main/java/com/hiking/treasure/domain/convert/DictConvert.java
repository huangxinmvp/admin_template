package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.Dict;
import com.hiking.treasure.domain.dto.create.DictCreateDTO;
import com.hiking.treasure.domain.dto.update.DictUpdateDTO;
import com.hiking.treasure.domain.dto.query.DictQueryDTO;
import com.hiking.treasure.domain.vo.DictVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface DictConvert {
    Dict toEntity(DictCreateDTO dto);

    Dict toEntity(DictUpdateDTO dto);

    Dict toEntity(DictQueryDTO dto);

    DictVO toVO(Dict entity);

    List<DictVO> toVOs(List<Dict> list);
}
