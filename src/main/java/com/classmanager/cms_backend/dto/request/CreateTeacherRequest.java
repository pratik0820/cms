package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateTeacherRequest {

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

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @PositiveOrZero(message = "Experience years cannot be negative")
    private Integer experienceYears;

    private List<@NotBlank(message = "Subject cannot be blank") String> subjects;

    private List<UUID> subjectIds;

    private List<UUID> courseIds;

    private List<UUID> batchIds;

    private String specialization;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotBlank(message = "Employment type is required")
    private String employmentType;

    private String salaryType;

    @PositiveOrZero(message = "Monthly salary cannot be negative")
    private BigDecimal monthlySalary;

    @PositiveOrZero(message = "Hourly rate cannot be negative")
    private BigDecimal hourlyRate;

    @NotBlank(message = "Login ID is required")
    private String loginId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotNull(message = "Branch is required")
    private UUID branchId;

    private String address;
}
