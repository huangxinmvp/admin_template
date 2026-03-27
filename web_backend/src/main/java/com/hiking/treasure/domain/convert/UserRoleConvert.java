package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.domain.dto.create.UserRoleCreateDTO;
import com.hiking.treasure.domain.dto.update.UserRoleUpdateDTO;
import com.hiking.treasure.domain.dto.query.UserRoleQueryDTO;
import com.hiking.treasure.domain.vo.UserRoleVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface UserRoleConvert {
    UserRole toEntity(UserRoleCreateDTO dto);

    UserRole toEntity(UserRoleUpdateDTO dto);

    UserRole toEntity(UserRoleQueryDTO dto);

    UserRoleVO toVO(UserRole entity);

    List<UserRoleVO> toVOs(List<UserRole> list);
}
