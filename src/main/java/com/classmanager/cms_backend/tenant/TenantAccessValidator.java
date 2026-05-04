package com.classmanager.cms_backend.tenant;

import com.classmanager.cms_backend.entity.BaseEntity;
import com.classmanager.cms_backend.util.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantAccessValidator {

    private static final Logger log = LogManager.getLogger(TenantAccessValidator.class);

    /**
     * Validates current tenant matches expected tenant
     */
    public void validateTenant(UUID resourceTenantId) {
        UUID currentTenant = SecurityUtils.getCurrentTenantId();

        if (!currentTenant.equals(resourceTenantId)) {
            log.error("Cross-tenant access attempt! currentTenant={} resourceTenant={}",
                    currentTenant, resourceTenantId);
            throw new SecurityException("Cross-tenant access denied");
        }
    }

    /**
     * Validate entity belongs to current tenant
     */
    public void validateSameTenant(BaseEntity entity) {
        validateTenant(entity.getTenantId());
    }

    /**
     * Validate multiple entities belong to same tenant
     */
    public void validateSameTenant(UUID tenantId1, UUID tenantId2) {
        if (!tenantId1.equals(tenantId2)) {
            throw new SecurityException("Entities belong to different tenants");
        }
    }
}
