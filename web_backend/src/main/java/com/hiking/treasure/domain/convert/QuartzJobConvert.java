package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.QuartzJob;
import com.hiking.treasure.domain.dto.create.QuartzJobCreateDTO;
import com.hiking.treasure.domain.dto.update.QuartzJobUpdateDTO;
import com.hiking.treasure.domain.dto.query.QuartzJobQueryDTO;
import com.hiking.treasure.domain.vo.QuartzJobVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface QuartzJobConvert {
    QuartzJob toEntity(QuartzJobCreateDTO dto);

    QuartzJob toEntity(QuartzJobUpdateDTO dto);

    QuartzJob toEntity(QuartzJobQueryDTO dto);

    QuartzJobVO toVO(QuartzJob entity);

    List<QuartzJobVO> toVOs(List<QuartzJob> list);
}
