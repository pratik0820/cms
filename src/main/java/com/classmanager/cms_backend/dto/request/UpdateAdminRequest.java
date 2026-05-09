package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateAdminRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private LocalDate dateOfBirth;

    private String gender;

    private String profilePhotoUrl;

    @NotBlank(message = "Login ID is required")
    private String loginId;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String confirmPassword;

    @NotBlank(message = "Role is required")
    private String role;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotBlank(message = "Access level is required")
    private String accessLevel;

    private UUID branchId;

    private String address;

    private Boolean allBranchesAccess;
}
