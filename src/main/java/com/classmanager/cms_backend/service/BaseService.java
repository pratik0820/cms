package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.repository.BaseRepository;
import com.classmanager.cms_backend.util.SecurityUtils;

import java.util.UUID;

public abstract class BaseService extends BaseRepository {

    protected UserService userService;

    protected void validateTenant(UUID resourceTenantId) {
        SecurityUtils.assertTenantAccess(resourceTenantId);
    }

    protected UUID getCurrentTenant() {
        return SecurityUtils.getCurrentTenantId();
    }
}
