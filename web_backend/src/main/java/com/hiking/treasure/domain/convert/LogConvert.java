package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.Log;
import com.hiking.treasure.domain.dto.create.LogCreateDTO;
import com.hiking.treasure.domain.dto.update.LogUpdateDTO;
import com.hiking.treasure.domain.dto.query.LogQueryDTO;
import com.hiking.treasure.domain.vo.LogVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface LogConvert {
    Log toEntity(LogCreateDTO dto);

    Log toEntity(LogUpdateDTO dto);

    Log toEntity(LogQueryDTO dto);

    LogVO toVO(Log entity);

    List<LogVO> toVOs(List<Log> list);
}
