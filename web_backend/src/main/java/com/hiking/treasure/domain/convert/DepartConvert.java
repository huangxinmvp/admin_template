package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.domain.dto.create.DepartCreateDTO;
import com.hiking.treasure.domain.dto.update.DepartUpdateDTO;
import com.hiking.treasure.domain.dto.query.DepartQueryDTO;
import com.hiking.treasure.domain.vo.DepartVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface DepartConvert {
    Depart toEntity(DepartCreateDTO dto);

    Depart toEntity(DepartUpdateDTO dto);

    Depart toEntity(DepartQueryDTO dto);

    DepartVO toVO(Depart entity);

    List<DepartVO> toVOs(List<Depart> list);
}
