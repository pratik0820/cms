package com.classmanager.cms_backend.security;

import com.classmanager.cms_backend.entity.Permission;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CmsUserDetailsTest {

    @Test
    void exposesRoleAndPermissionAuthorities() {
        Permission permission = Permission.builder()
                .id(UUID.randomUUID())
                .name("VIEW_STUDENT")
                .build();

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .permissions(Set.of(permission))
                .build();

        User user = User.builder()
                .email("admin@example.com")
                .loginId("adm-001")
                .passwordHash("secret")
                .fullName("Admin User")
                .roles(Set.of(role))
                .isActive(true)
                .build();

        CmsUserDetails userDetails = new CmsUserDetails(user);

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "VIEW_STUDENT");
        assertThat(userDetails.getLoginId()).isEqualTo("adm-001");
    }
}
