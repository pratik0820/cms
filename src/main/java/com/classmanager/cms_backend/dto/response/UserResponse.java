package com.classmanager.cms_backend.dto.response;


import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTOs for user-related endpoints.
 * Separate shapes per role — only expose fields relevant to each role.
 */
public class UserResponse {

    // ─── ADMIN ───────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AdminResponse {
        private UUID id;
        private String fullName;
        private String email;
        private String phone;
        private UserRole role;
        private UUID branchId;
        private String branchName;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
    }

    // ─── TEACHER ─────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TeacherResponse {
        private UUID id;
        private UUID userId;           // linked users table ID
        private String fullName;
        private String email;
        private String phone;
        private String qualification;
        private LocalDate joiningDate;
        private BigDecimal hourlyRate;
        private UUID branchId;
        private String branchName;
        private Boolean isActive;
        private LocalDateTime createdAt;

        /**
         * Auto-generated credentials — ONLY returned in the creation response.
         * Never returned in subsequent GET calls.
         */
        private String generatedLoginEmail;   // same as email — teacher logs in with email
        private String generatedPassword;     // temporary password; null after creation response
    }

    // ─── STUDENT ─────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StudentResponse {
        private UUID id;
        private UUID userId;
        private String name;
        private String loginId;        // e.g. STU-A3X9KL
        private String email;
        private String mobile;
        private String parentName;
        private String parentPhone;
        private String gender;
        private String standard;
        private BoardType board;
        private String schoolName;
        private String photoUrl;
        private Boolean isAdmissionFinal;
        private UUID branchId;
        private String branchName;
        private UUID batchId;
        private String batchName;
        private Boolean isActive;
        private LocalDateTime createdAt;

        /** Returned ONLY in the creation response — never on subsequent GETs */
        private String generatedLoginId;
        private String generatedPassword;
        private String parentLoginNote;   // "Parent can use the same login_id and password"
    }

    // ─── GENERIC USER (for simple lookups) ───────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SimpleUserResponse {
        private UUID id;
        private String fullName;
        private String email;
        private UserRole role;
        private Boolean isActive;
    }
}
