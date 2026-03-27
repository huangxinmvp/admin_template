package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.QuartzJobLog;
import com.hiking.treasure.domain.dto.create.QuartzJobLogCreateDTO;
import com.hiking.treasure.domain.dto.update.QuartzJobLogUpdateDTO;
import com.hiking.treasure.domain.dto.query.QuartzJobLogQueryDTO;
import com.hiking.treasure.domain.vo.QuartzJobLogVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface QuartzJobLogConvert {
    QuartzJobLog toEntity(QuartzJobLogCreateDTO dto);

    QuartzJobLog toEntity(QuartzJobLogUpdateDTO dto);

    QuartzJobLog toEntity(QuartzJobLogQueryDTO dto);

    QuartzJobLogVO toVO(QuartzJobLog entity);

    List<QuartzJobLogVO> toVOs(List<QuartzJobLog> list);
}
