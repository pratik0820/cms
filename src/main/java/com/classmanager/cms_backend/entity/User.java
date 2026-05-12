package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_login_id", columnList = "login_id")
})
public class User extends BaseEntity {

    @Column(name = "email")
    private String email;

    @Column(name = "login_id", columnDefinition = "varchar(255)")
    private String loginId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void recordFailedLogin(int maxAttempts, int lockMinutes) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockMinutes);
        }
    }
}
