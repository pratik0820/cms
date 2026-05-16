package com.classmanager.cms_backend.entity;

import com.classmanager.cms_backend.enums.EnrolmentStatus;
import com.classmanager.cms_backend.enums.FeePaymentPlan;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Records a student's enrolment into a {@link Batch} for a specific
 * academic year, along with the subjects they are taking and the
 * fee agreed upon by the admin.
 *
 * Key design decisions:
 *   - {@code subjectGroup} is optional — records which group the admin
 *     started from, but the actual subjects are in {@code subjects}.
 *   - {@code agreedTotalFee} is entered manually by the admin.
 *   - {@code instalments} is the admin-entered payment schedule.
 *   - No backend fee calculation is performed.
 */
@Entity
@Table(name = "student_enrolments", indexes = {
        @Index(name = "idx_enrolments_student",       columnList = "student_id"),
        @Index(name = "idx_enrolments_batch",          columnList = "batch_id"),
        @Index(name = "idx_enrolments_academic_year",  columnList = "academic_year"),
        @Index(name = "idx_enrolments_status",         columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentEnrolment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    /** Denormalised from batch for fast filtering. */
    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    /** Optional: subject group selected as a starting point. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id")
    private SubjectGroup subjectGroup;

    /** Actual subjects this student is enrolled for. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "enrolment_subjects",
            joinColumns = @JoinColumn(name = "enrolment_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();

    /** Total fee agreed by the admin — entered manually. */
    @Column(name = "agreed_total_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedTotalFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_plan", nullable = false, length = 20)
    private FeePaymentPlan paymentPlan;

    @Column(name = "enrolment_date", nullable = false)
    private LocalDate enrolmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnrolmentStatus status = EnrolmentStatus.ACTIVE;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "enrolment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("instalmentNumber ASC")
    @Builder.Default
    private List<EnrolmentInstalment> instalments = new ArrayList<>();
}
