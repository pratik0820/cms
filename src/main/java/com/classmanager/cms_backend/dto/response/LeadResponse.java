package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

    private UUID id;
    private String leadCode;

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
    private String preferredBranchName;

    /** NEW_ADMISSION, TRANSFER, OTHER */
    private String inquiryFor;

    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private List<SubjectResponse> subjects;
    private String expectedAdmissionYear;
    private LocalDate preferredAdmissionDate;
    private String preferredContactTime;
    private String modeOfContact;
    private String bestDaysToContact;

    // ─── Counsellor's Recommendation ──────────────────────────────────────────

    private String courseRecommended;
    private List<SubjectResponse> recommendedSubjects;
    private String subjectsSuggested;
    private String batchSuggested;
    private String admissionLikelihood;
    private String remarks;
    private LocalDateTime nextFollowUpAt;

    private UUID counsellorUserId;
    private String counsellorName;

    // ─── System / Status ──────────────────────────────────────────────────────

    private String status;
    private UUID assignedToUserId;
    private String assignedToName;
    private UUID createdByUserId;
    private String createdByName;
    private UUID convertedStudentId;
    private LocalDateTime convertedAt;
    private List<FollowUpResponse> followUps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    private List<InstalmentResponse> installments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstalmentResponse {
        private UUID id;
        private Integer instalmentNumber;
        private BigDecimal amount;
        private LocalDate dueDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowUpResponse {
        private UUID id;
        private LocalDateTime followUpAt;
        private String modeOfContact;
        private String notes;
        private LocalDateTime nextFollowUpAt;
        private String statusAfter;
        private String createdByName;

        private String spokeWith;
        private String remarks;
        private String nextFollowUpType;
        private String nextFollowUpMode;
        private UUID nextFollowUpByUserId;
        private String nextFollowUpByName;
        private String reminder;
        private String priority;
        private String nextFollowUpNotes;
    }
}
