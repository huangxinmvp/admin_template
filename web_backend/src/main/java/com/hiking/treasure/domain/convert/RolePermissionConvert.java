package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.RolePermission;
import com.hiking.treasure.domain.dto.create.RolePermissionCreateDTO;
import com.hiking.treasure.domain.dto.update.RolePermissionUpdateDTO;
import com.hiking.treasure.domain.dto.query.RolePermissionQueryDTO;
import com.hiking.treasure.domain.vo.RolePermissionVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface RolePermissionConvert {
    RolePermission toEntity(RolePermissionCreateDTO dto);

    RolePermission toEntity(RolePermissionUpdateDTO dto);

    RolePermission toEntity(RolePermissionQueryDTO dto);

    RolePermissionVO toVO(RolePermission entity);

    List<RolePermissionVO> toVOs(List<RolePermission> list);
}
