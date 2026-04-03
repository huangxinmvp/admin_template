package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.AgentRoleCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.AgentRoleQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.AgentRoleUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleVO;
import com.hiking.treasure.modules.phase1.entity.AgentRole;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AgentRoleConvert {

    AgentRole toEntity(AgentRoleCreateDTO dto);

    AgentRole toEntity(AgentRoleUpdateDTO dto);

    AgentRole toEntity(AgentRoleQueryDTO dto);

    AgentRoleVO toVO(AgentRole entity);

    List<AgentRoleVO> toVOs(List<AgentRole> entities);
}
