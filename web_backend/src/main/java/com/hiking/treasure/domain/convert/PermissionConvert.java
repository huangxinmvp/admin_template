package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.Permission;
import com.hiking.treasure.domain.dto.create.PermissionCreateDTO;
import com.hiking.treasure.domain.dto.update.PermissionUpdateDTO;
import com.hiking.treasure.domain.dto.query.PermissionQueryDTO;
import com.hiking.treasure.domain.vo.PermissionVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface PermissionConvert {
    Permission toEntity(PermissionCreateDTO dto);

    Permission toEntity(PermissionUpdateDTO dto);

    Permission toEntity(PermissionQueryDTO dto);

    PermissionVO toVO(Permission entity);

    List<PermissionVO> toVOs(List<Permission> list);
}
