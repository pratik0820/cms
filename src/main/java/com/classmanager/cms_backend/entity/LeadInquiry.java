package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lead_inquiries", indexes = {
        @Index(name = "idx_lead_inquiries_branch", columnList = "preferred_branch_id"),
        @Index(name = "idx_lead_inquiries_status", columnList = "status"),
        @Index(name = "idx_lead_inquiries_source", columnList = "lead_source"),
        @Index(name = "idx_lead_inquiries_follow_up", columnList = "next_follow_up_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadInquiry extends BaseEntity {

    @Column(name = "lead_code", nullable = false, unique = true, length = 40)
    private String leadCode;

    // ─── Student Information ───────────────────────────────────────────────────

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "gender", length = 40)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_group", length = 20)
    private String bloodGroup;

    @Column(name = "class_interested_in")
    private String classInterestedIn;

    @Column(name = "board", length = 40)
    private String board;

    @Column(name = "medium", length = 80)
    private String medium;

    @Column(name = "stream", length = 80)
    private String stream;

    @Column(name = "current_school", length = 255)
    private String currentSchool;

    @Column(name = "last_class_completed", length = 80)
    private String lastClassCompleted;

    @Column(name = "last_exam_percentage", length = 80)
    private String lastExamPercentage;

    @Column(name = "address", length = 2000)
    private String address;

    // ─── Contact Information ───────────────────────────────────────────────────

    @Column(name = "mobile_country_code", length = 10)
    private String mobileCountryCode;

    @Column(name = "mobile_number", nullable = false, length = 40)
    private String mobileNumber;

    @Column(name = "alternate_mobile_country_code", length = 10)
    private String alternateMobileCountryCode;

    @Column(name = "alternate_mobile_number", length = 40)
    private String alternateMobileNumber;

    @Column(name = "email")
    private String email;

    // ─── Parent / Guardian Information ────────────────────────────────────────

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "mother_name")
    private String motherName;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "relation", length = 80)
    private String relation;

    @Column(name = "father_mobile_country_code", length = 10)
    private String fatherMobileCountryCode;

    @Column(name = "father_mobile_number", length = 40)
    private String fatherMobileNumber;

    @Column(name = "mother_mobile_country_code", length = 10)
    private String motherMobileCountryCode;

    @Column(name = "mother_mobile_number", length = 40)
    private String motherMobileNumber;

    @Column(name = "guardian_mobile_country_code", length = 10)
    private String guardianMobileCountryCode;

    @Column(name = "guardian_mobile_number", length = 40)
    private String guardianMobileNumber;

    @Column(name = "parent_email")
    private String parentEmail;

    @Column(name = "father_occupation")
    private String fatherOccupation;

    @Column(name = "mother_occupation")
    private String motherOccupation;

    @Column(name = "annual_income", length = 80)
    private String annualIncome;

    @Column(name = "nationality", length = 80)
    private String nationality;

    // ─── Lead Source & Inquiry Details ────────────────────────────────────────

    @Column(name = "lead_source", nullable = false, length = 120)
    private String leadSource;

    @Column(name = "referred_by")
    private String referredBy;

    @Column(name = "heard_about_us")
    private String heardAboutUs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_branch_id")
    private Branch preferredBranch;

    /** Inquiry type: NEW_ADMISSION, TRANSFER, OTHER */
    @Column(name = "inquiry_for", length = 80)
    private String inquiryFor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "lead_subjects",
            joinColumns = @JoinColumn(name = "lead_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();

    @Column(name = "expected_admission_year", length = 20)
    private String expectedAdmissionYear;

    @Column(name = "preferred_admission_date")
    private LocalDate preferredAdmissionDate;

    @Column(name = "preferred_contact_time", length = 80)
    private String preferredContactTime;

    @Column(name = "mode_of_contact", length = 80)
    private String modeOfContact;

    @Column(name = "best_days_to_contact", length = 120)
    private String bestDaysToContact;

    // ─── Counsellor's Recommendation ──────────────────────────────────────────

    @Column(name = "course_recommended")
    private String courseRecommended;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "lead_recommended_subjects",
            joinColumns = @JoinColumn(name = "lead_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private List<Subject> recommendedSubjects = new ArrayList<>();

    /** Free-text fallback for subjects suggested by counsellor */
    @Column(name = "subjects_suggested", length = 2000)
    private String subjectsSuggested;

    @Column(name = "batch_suggested")
    private String batchSuggested;

    @Column(name = "admission_likelihood", length = 80)
    private String admissionLikelihood;

    @Column(name = "remarks", length = 2000)
    private String remarks;

    @Column(name = "next_follow_up_at")
    private LocalDateTime nextFollowUpAt;

    /** The counsellor who completed section 4 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counsellor_user_id")
    private User counsellorUser;

    // ─── System / Status Fields ────────────────────────────────────────────────

    @Column(name = "status", nullable = false, length = 40)
    @Builder.Default
    private String status = "NEW";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_student_id")
    private Student convertedStudent;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;
}
