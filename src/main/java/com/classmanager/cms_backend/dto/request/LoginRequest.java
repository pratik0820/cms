package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    /**
     * Backward-compatible staff login field. Admins and teachers use email.
     * Student and parent portals should send identifier with the generated loginId.
     */
    private String email;

    /** Email for staff, or generated student loginId for student/parent shared login. */
    private String identifier;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /** Optional client FCM token for push notifications. */
    private String fcmToken;

    public String getLoginIdentifier() {
        if (identifier != null && !identifier.isBlank()) {
            return identifier;
        }
        return email;
    }
}
