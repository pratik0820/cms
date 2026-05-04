package com.classmanager.cms_backend.security;

import com.classmanager.cms_backend.entity.Permission;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
public class CmsUserDetails implements UserDetails {

    private final UUID userId;
    private final UUID tenantId;
    private final String email;
    private final String passwordHash;
    private final UUID branchId;
    private final boolean active;
    private final boolean accountLocked;

    private final User user;

    public CmsUserDetails(User user) {
        this.user = user;
        this.userId       = user.getId();
        this.tenantId     = user.getTenantId();
        this.email        = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.branchId     = user.getBranch() != null ? user.getBranch().getId() : null;
        this.active       = Boolean.TRUE.equals(user.getIsActive());
        this.accountLocked = user.isAccountLocked();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(
                                new SimpleGrantedAuthority(permission.getName())
                        );
                    }
                }
            }
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public Set<Role> getRoles() {
        return user.getRoles();
    }
}
