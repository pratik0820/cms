package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.FeePaymentPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateStudentRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Branch is required")
    private UUID branchId;

    @NotBlank(message = "Standard is required")
    private String standard;

    private String batch;

    private UUID courseId;

    private UUID batchId;

    private UUID subjectGroupId;

    private List<UUID> subjectIds;

    @DecimalMin(value = "0.0", message = "Fee cannot be negative")
    private BigDecimal agreedTotalFee;

    private FeePaymentPlan paymentPlan;

    @Valid
    private List<CreateEnrolmentRequest.InstalmentRequest> instalments;

    private String enrolmentNotes;

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

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    private String profilePhotoUrl;
}
