package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.domain.dto.command.AgentRoleCenterSaveDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.AgentRoleCenterQueryDTO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleCenterDetailVO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleCenterListVO;
import com.hiking.treasure.modules.phase1.entity.AgentRole;

public interface AgentRoleService extends IService<AgentRole> {

    Page<AgentRoleCenterListVO> pageAgentRoleCenter(AgentRoleCenterQueryDTO dto, long pageNo, long pageSize);

    AgentRoleCenterDetailVO getAgentRoleCenterDetail(String id);

    AgentRoleCenterDetailVO createAgentRoleCenter(AgentRoleCenterSaveDTO dto);

    AgentRoleCenterDetailVO updateAgentRoleCenter(String id, AgentRoleCenterSaveDTO dto);
}
