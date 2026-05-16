package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String mobileNumber;
    private String alternateMobileNumber;
    private String email;
    private String fatherName;
    private String motherName;
    private String guardianName;
    private String relation;
    private String fatherMobileNumber;
    private String motherMobileNumber;
    private String guardianMobileNumber;
    private String parentEmail;
    private String fatherOccupation;
    private String motherOccupation;
    private String annualIncome;
    private String nationality;
    private String leadSource;
    private String referredBy;
    private String heardAboutUs;
    private UUID preferredBranchId;
    private String preferredBranchName;
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
    private String courseRecommended;
    private String batchSuggested;
    private String admissionLikelihood;
    private String remarks;
    private LocalDateTime nextFollowUpAt;
    private String status;
    private UUID assignedToUserId;
    private String assignedToName;
    private UUID convertedStudentId;
    private LocalDateTime convertedAt;
    private List<FollowUpResponse> followUps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
    }
}
