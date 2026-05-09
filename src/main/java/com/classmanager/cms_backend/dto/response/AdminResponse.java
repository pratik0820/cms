package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String loginId;
    private LocalDate dateOfBirth;
    private String gender;
    private String profilePhotoUrl;
    private String role;
    private LocalDate joiningDate;
    private String accessLevel;
    private String address;
    private Boolean allBranchesAccess;
    private UUID branchId;
    private String branchName;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
