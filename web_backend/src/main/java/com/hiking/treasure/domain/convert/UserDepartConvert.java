package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.domain.dto.create.UserDepartCreateDTO;
import com.hiking.treasure.domain.dto.update.UserDepartUpdateDTO;
import com.hiking.treasure.domain.dto.query.UserDepartQueryDTO;
import com.hiking.treasure.domain.vo.UserDepartVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface UserDepartConvert {
    UserDepart toEntity(UserDepartCreateDTO dto);

    UserDepart toEntity(UserDepartUpdateDTO dto);

    UserDepart toEntity(UserDepartQueryDTO dto);

    UserDepartVO toVO(UserDepart entity);

    List<UserDepartVO> toVOs(List<UserDepart> list);
}
