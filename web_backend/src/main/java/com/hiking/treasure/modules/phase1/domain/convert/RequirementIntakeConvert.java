package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.RequirementIntakeCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.RequirementIntakeQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.RequirementIntakeUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.RequirementIntakeVO;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequirementIntakeConvert {

    RequirementIntake toEntity(RequirementIntakeCreateDTO dto);

    RequirementIntake toEntity(RequirementIntakeUpdateDTO dto);

    RequirementIntake toEntity(RequirementIntakeQueryDTO dto);

    RequirementIntakeVO toVO(RequirementIntake entity);

    List<RequirementIntakeVO> toVOs(List<RequirementIntake> entities);
}
