package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateLeadRequest {

    // ─── Student Information ───────────────────────────────────────────────────

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

    // ─── Contact Information ───────────────────────────────────────────────────

    private String mobileCountryCode;
    private String mobileNumber;
    private String alternateMobileCountryCode;
    private String alternateMobileNumber;

    @Email(message = "Please provide a valid email address")
    private String email;

    // ─── Parent / Guardian Information ────────────────────────────────────────

    private String fatherName;
    private String motherName;
    private String guardianName;
    private String relation;

    private String fatherMobileCountryCode;
    private String fatherMobileNumber;

    private String motherMobileCountryCode;
    private String motherMobileNumber;

    private String guardianMobileCountryCode;
    private String guardianMobileNumber;

    @Email(message = "Please provide a valid parent email address")
    private String parentEmail;

    private String fatherOccupation;
    private String motherOccupation;
    private String annualIncome;
    private String nationality;

    // ─── Lead Source & Inquiry Details ────────────────────────────────────────

    private String leadSource;
    private String referredBy;
    private String heardAboutUs;
    private UUID preferredBranchId;

    /** NEW_ADMISSION, TRANSFER, OTHER */
    private String inquiryFor;

    private UUID courseId;
    private UUID batchId;
    private List<UUID> subjectIds;
    private String expectedAdmissionYear;
    private LocalDate preferredAdmissionDate;
    private String preferredContactTime;
    private String modeOfContact;
    private String bestDaysToContact;

    // ─── Counsellor's Recommendation ──────────────────────────────────────────

    private String courseRecommended;

    /** UUIDs of subjects recommended by counsellor (from subjects catalogue) */
    private List<UUID> recommendedSubjectIds;

    /** Free-text subjects suggested (if not from catalogue) */
    private String subjectsSuggested;

    private String batchSuggested;
    private String admissionLikelihood;
    private String remarks;
    private LocalDateTime nextFollowUpAt;

    /** UUID of the counsellor user */
    private UUID counsellorUserId;

    // ─── Assignment ───────────────────────────────────────────────────────────

    private UUID assignedToUserId;

    // ─── Fee & Payment Structure ───────────────────────────────────────────────

    private BigDecimal totalBaseFee;
    private String meritScholarship;
    private BigDecimal additionalConcessionAmount;
    private String additionalCategoryName;
    private BigDecimal additionalCategoryDiscountAmount;
    private BigDecimal finalPayableFee;
    private BigDecimal tokenAmountPaid;
    private String modeOfPayment;
    private String paymentStructure;
    private Boolean financialAssistanceRequired;
    private Boolean externalScholarshipApplicable;
    private String externalScholarshipDetails;
    private String tokenPaymentMode;
    private String tokenRemarks;

    private List<CreateLeadRequest.InstalmentRequest> installments;
}
