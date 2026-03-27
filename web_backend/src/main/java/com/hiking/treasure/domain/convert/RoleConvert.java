package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.domain.dto.create.RoleCreateDTO;
import com.hiking.treasure.domain.dto.update.RoleUpdateDTO;
import com.hiking.treasure.domain.dto.query.RoleQueryDTO;
import com.hiking.treasure.domain.vo.RoleVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface RoleConvert {
    Role toEntity(RoleCreateDTO dto);

    Role toEntity(RoleUpdateDTO dto);

    Role toEntity(RoleQueryDTO dto);

    RoleVO toVO(Role entity);

    List<RoleVO> toVOs(List<Role> list);
}
