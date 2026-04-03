package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.modules.phase1.domain.convert.AgentRoleConvert;
import com.hiking.treasure.modules.phase1.domain.dto.create.AgentRoleCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.AgentRoleQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.AgentRoleUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.AgentRoleVO;
import com.hiking.treasure.modules.phase1.entity.AgentRole;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentRoleCategory;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentRoleStatus;
import com.hiking.treasure.modules.phase1.service.AgentRoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AICoOS Agent 角色", description = "AICoOS Agent 角色基础 CRUD 接口")
@RestController
@RequestMapping("/api/aicoos/agentRole")
@PreAuthorize("hasRole('ADMIN')")
public class AgentRoleController extends AbstractPhase1CrudController<
        AgentRole, AgentRoleService, AgentRoleCreateDTO, AgentRoleUpdateDTO, AgentRoleQueryDTO, AgentRoleVO> {

    @Resource
    private AgentRoleService agentRoleService;

    @Resource
    private AgentRoleConvert agentRoleConvert;

    @Override
    protected AgentRole toCreateEntity(AgentRoleCreateDTO dto) {
        return agentRoleConvert.toEntity(dto);
    }

    @Override
    protected AgentRole toUpdateEntity(AgentRoleUpdateDTO dto) {
        return agentRoleConvert.toEntity(dto);
    }

    @Override
    protected AgentRole toQueryEntity(AgentRoleQueryDTO dto) {
        return agentRoleConvert.toEntity(dto);
    }

    @Override
    protected AgentRoleVO toVO(AgentRole entity) {
        return agentRoleConvert.toVO(entity);
    }

    @Override
    protected List<AgentRoleVO> toVOs(List<AgentRole> entities) {
        return agentRoleConvert.toVOs(entities);
    }

    @Override
    protected void setEntityId(AgentRole entity, String id) {
        entity.setId(id);
    }

    @Override
    protected void applyCreateDefaults(AgentRole entity) {
        if (entity.getRoleCategory() == null || entity.getRoleCategory().isBlank()) {
            entity.setRoleCategory(AgentRoleCategory.DELIVERY.getCode());
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus(AgentRoleStatus.ACTIVE.getCode());
        }
        if (entity.getDefaultFlag() == null) {
            entity.setDefaultFlag(0);
        }
        if (entity.getApprovalCollaborationFlag() == null) {
            entity.setApprovalCollaborationFlag(1);
        }
        if (entity.getApprovalRequired() == null) {
            entity.setApprovalRequired(1);
        }
        if (entity.getMaxConcurrency() == null) {
            entity.setMaxConcurrency(1);
        }
    }

    @Override
    protected AgentRoleService service() {
        return agentRoleService;
    }
}
