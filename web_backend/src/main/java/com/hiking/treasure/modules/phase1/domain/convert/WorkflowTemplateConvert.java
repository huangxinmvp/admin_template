package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.WorkflowTemplateCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.WorkflowTemplateQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.WorkflowTemplateUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.WorkflowTemplateVO;
import com.hiking.treasure.modules.phase1.entity.WorkflowTemplate;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkflowTemplateConvert {

    WorkflowTemplate toEntity(WorkflowTemplateCreateDTO dto);

    WorkflowTemplate toEntity(WorkflowTemplateUpdateDTO dto);

    WorkflowTemplate toEntity(WorkflowTemplateQueryDTO dto);

    WorkflowTemplateVO toVO(WorkflowTemplate entity);

    List<WorkflowTemplateVO> toVOs(List<WorkflowTemplate> entities);
}
