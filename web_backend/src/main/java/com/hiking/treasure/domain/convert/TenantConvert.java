package com.hiking.treasure.domain.convert;

import com.hiking.treasure.domain.dto.create.TenantCreateDTO;
import com.hiking.treasure.domain.dto.query.TenantQueryDTO;
import com.hiking.treasure.domain.dto.update.TenantUpdateDTO;
import com.hiking.treasure.domain.vo.TenantVO;
import com.hiking.treasure.entity.Tenant;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TenantConvert {
    Tenant toEntity(TenantCreateDTO dto);

    Tenant toEntity(TenantUpdateDTO dto);

    Tenant toEntity(TenantQueryDTO dto);

    TenantVO toVO(Tenant entity);

    List<TenantVO> toVOs(List<Tenant> entities);
}
