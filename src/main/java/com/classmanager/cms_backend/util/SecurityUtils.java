package com.classmanager.cms_backend.util;

import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<CmsUserDetails> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CmsUserDetails userDetails) {
            return Optional.of(userDetails);
        }
        return Optional.empty();
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser()
                .map(CmsUserDetails::getUserId)
                .orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    public static UUID getCurrentTenantId() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return getCurrentUser()
                    .map(CmsUserDetails::getTenantId)
                    .orElseThrow(() -> new IllegalStateException("No tenant context available"));
        }
        return tenantId;
    }

    public static Set<String> getCurrentRoles() {
        return getCurrentUser()
                .map(user -> user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    public static boolean hasRole(String roleName) {
        return getCurrentUser()
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(roleName)))
                .orElse(false);
    }

    public static boolean isAdminOrAbove() {
        return getCurrentUser()
                .map(user -> user.getRoles().stream()
                        .anyMatch(role ->
                                role.getName().equals(UserRole.PLATFORM_ADMIN.name()) ||
                                        role.getName().equals(UserRole.TENANT_OWNER.name()) ||
                                role.getName().equals(UserRole.ADMIN.name()) ||
                                        role.getName().equals(UserRole.SUPER_ADMIN.name())))
                .orElse(false);
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof CmsUserDetails;
    }

    /** Verifies the current tenant matches the expected tenant. Used in data access guards. */
    public static void assertTenantAccess(UUID resourceTenantId) {
        UUID currentTenant = getCurrentTenantId();
        if (!currentTenant.equals(resourceTenantId)) {
            throw new SecurityException("Cross-tenant access denied");
        }
    }
}
