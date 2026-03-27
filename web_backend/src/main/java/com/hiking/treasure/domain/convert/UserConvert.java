package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.domain.dto.create.UserCreateDTO;
import com.hiking.treasure.domain.dto.update.UserUpdateDTO;
import com.hiking.treasure.domain.dto.query.UserQueryDTO;
import com.hiking.treasure.domain.vo.UserVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface UserConvert {
    User toEntity(UserCreateDTO dto);

    User toEntity(UserUpdateDTO dto);

    User toEntity(UserQueryDTO dto);

    UserVO toVO(User entity);

    List<UserVO> toVOs(List<User> list);
}
