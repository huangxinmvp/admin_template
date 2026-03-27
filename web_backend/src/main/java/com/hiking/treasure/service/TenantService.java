package com.hiking.treasure.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.entity.Tenant;

public interface TenantService extends IService<Tenant> {
    Tenant getByCode(String tenantCode);

    Tenant requireActiveTenant(String tenantId);

    boolean deleteTenantById(String tenantId);

    boolean deleteTenantsByIds(java.util.List<String> tenantIds);
}
