package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.PermissionDataRule;
import com.hiking.treasure.domain.dto.create.PermissionDataRuleCreateDTO;
import com.hiking.treasure.domain.dto.update.PermissionDataRuleUpdateDTO;
import com.hiking.treasure.domain.dto.query.PermissionDataRuleQueryDTO;
import com.hiking.treasure.domain.vo.PermissionDataRuleVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface PermissionDataRuleConvert {
    PermissionDataRule toEntity(PermissionDataRuleCreateDTO dto);

    PermissionDataRule toEntity(PermissionDataRuleUpdateDTO dto);

    PermissionDataRule toEntity(PermissionDataRuleQueryDTO dto);

    PermissionDataRuleVO toVO(PermissionDataRule entity);

    List<PermissionDataRuleVO> toVOs(List<PermissionDataRule> list);
}
