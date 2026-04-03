package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.vo.ProjectToolBindingVO;
import com.hiking.treasure.modules.phase1.entity.ProjectToolBinding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectToolBindingConvert {

    public ProjectToolBindingVO toVO(ProjectToolBinding entity) {
        if (entity == null) {
            return null;
        }
        ProjectToolBindingVO vo = new ProjectToolBindingVO();
        vo.setId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setToolType(entity.getToolType());
        vo.setBindingType(entity.getBindingType());
        vo.setExternalId(entity.getExternalId());
        vo.setExternalKey(entity.getExternalKey());
        vo.setExternalName(entity.getExternalName());
        vo.setExternalUrl(entity.getExternalUrl());
        vo.setBindingStatus(entity.getBindingStatus());
        vo.setDefaultFlag(entity.getDefaultFlag());
        vo.setMetadataJson(entity.getMetadataJson());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    public List<ProjectToolBindingVO> toVOs(List<ProjectToolBinding> entities) {
        return entities == null ? List.of() : entities.stream().map(this::toVO).toList();
    }
}
