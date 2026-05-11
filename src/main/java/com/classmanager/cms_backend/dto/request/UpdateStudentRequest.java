package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateStudentRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Branch is required")
    private UUID branchId;

    @NotBlank(message = "Standard is required")
    private String standard;

    @NotBlank(message = "Batch is required")
    private String batch;

    private String gender;

    private LocalDate dateOfBirth;

    @NotBlank(message = "Mobile number is required")
    private String mobile;

    @NotBlank(message = "Parent name is required")
    private String parentName;

    @NotBlank(message = "Parent phone is required")
    private String parentPhone;

    @Email(message = "Please provide a valid email address")
    private String email;

    private String address;

    private String schoolName;

    @NotBlank(message = "Board is required")
    private String board;

    @NotNull(message = "Admission date is required")
    private LocalDate admissionDate;

    @NotBlank(message = "Login ID is required")
    private String loginId;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String confirmPassword;

    private String profilePhotoUrl;
}
