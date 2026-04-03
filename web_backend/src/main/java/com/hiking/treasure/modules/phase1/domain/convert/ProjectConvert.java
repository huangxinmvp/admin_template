package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.ProjectCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ProjectQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.ProjectUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectVO;
import com.hiking.treasure.modules.phase1.entity.Project;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectConvert {

    Project toEntity(ProjectCreateDTO dto);

    Project toEntity(ProjectUpdateDTO dto);

    Project toEntity(ProjectQueryDTO dto);

    ProjectVO toVO(Project entity);

    List<ProjectVO> toVOs(List<Project> entities);
}
