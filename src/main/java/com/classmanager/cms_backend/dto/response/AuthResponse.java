package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // JWT access token — short lived (15 min)
    private String accessToken;

    // Opaque refresh token — long lived (7 days), stored in DB
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long accessTokenExpiresIn;  // seconds until expiry

    // User info embedded for frontend convenience
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private String loginId;
        private String fullName;
        private List<String> roles;
        private UUID branchId;
        private String branchName;
    }
}
