package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateLeadRequest {

    @NotBlank(message = "Student name is required")
    private String studentName;

    private String gender;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String classInterestedIn;
    private String board;
    private String medium;
    private String stream;
    private String currentSchool;
    private String lastClassCompleted;
    private String lastExamPercentage;
    private String address;

    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    private String alternateMobileNumber;

    @Email(message = "Please provide a valid email address")
    private String email;

    private String fatherName;
    private String motherName;
    private String guardianName;
    private String relation;
    private String fatherMobileNumber;
    private String motherMobileNumber;
    private String guardianMobileNumber;

    @Email(message = "Please provide a valid parent email address")
    private String parentEmail;

    private String fatherOccupation;
    private String motherOccupation;
    private String annualIncome;
    private String nationality;

    @NotBlank(message = "Lead source is required")
    private String leadSource;

    private String referredBy;
    private String heardAboutUs;
    private UUID preferredBranchId;
    private UUID courseId;
    private UUID batchId;
    private List<UUID> subjectIds;
    private String expectedAdmissionYear;
    private LocalDate preferredAdmissionDate;
    private String preferredContactTime;
    private String modeOfContact;
    private String bestDaysToContact;
    private String courseRecommended;
    private String batchSuggested;
    private String admissionLikelihood;
    private String remarks;
    private LocalDateTime nextFollowUpAt;
    private UUID assignedToUserId;
}
