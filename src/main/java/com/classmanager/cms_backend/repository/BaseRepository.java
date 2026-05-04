package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public abstract class BaseRepository {

    public CmsUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CmsUserDetails userDetails) {
            return userDetails;
        }
        throw new IllegalStateException("No authenticated user in security context");
    }

    public UUID getCurrentUserId() {
        return getCurrentUserDetails().getUserId();
    }

    public UUID getCurrentTenantId() {
        return TenantContext.getCurrentTenant();
    }

}
