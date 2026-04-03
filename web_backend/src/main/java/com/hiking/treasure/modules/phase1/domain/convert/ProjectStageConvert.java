package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.ProjectStageCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ProjectStageQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.ProjectStageUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectStageVO;
import com.hiking.treasure.modules.phase1.entity.ProjectStage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectStageConvert {

    ProjectStage toEntity(ProjectStageCreateDTO dto);

    ProjectStage toEntity(ProjectStageUpdateDTO dto);

    ProjectStage toEntity(ProjectStageQueryDTO dto);

    ProjectStageVO toVO(ProjectStage entity);

    List<ProjectStageVO> toVOs(List<ProjectStage> entities);
}
