package com.classmanager.cms_backend.entity;

import com.classmanager.cms_backend.enums.BatchTiming;
import com.classmanager.cms_backend.enums.BatchType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A Batch is a running instance of a {@link Course} at a specific {@link Branch}
 * for a given academic year and timing slot.
 *
 * Students are enrolled into a Batch (not directly into a Course).
 * The fee is entered manually at enrolment time.
 */
@Entity
@Table(name = "batches", indexes = {
        @Index(name = "idx_batches_branch",        columnList = "branch_id"),
        @Index(name = "idx_batches_course",        columnList = "course_id"),
        @Index(name = "idx_batches_academic_year", columnList = "academic_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Display name, e.g. "8th CBSE Evening 2026-27". Auto-generated if blank. */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    /** Batch type / tier (Chanakya / Drona / Vyasa / Arjuna / TIER_E). */
    @Enumerated(EnumType.STRING)
    @Column(name = "batch_type", length = 20)
    private BatchType batchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "timing", nullable = false, length = 10)
    private BatchTiming timing;

    /** Custom timing label when timing=CUSTOM, e.g. "04:30 PM – 07:30 PM". */
    @Column(name = "timing_label", length = 60)
    private String timingLabel;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    /** Comma-separated days, e.g. "MON,TUE,WED,THU,FRI,SAT". */
    @Column(name = "days_of_week", length = 60)
    private String daysOfWeek;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Maximum students allowed. 0 = unlimited. */
    @Column(name = "max_students", nullable = false)
    @Builder.Default
    private Integer maxStudents = 0;

    /** Denormalised enrolled count — updated on enrolment changes. */
    @Column(name = "enrolled_count", nullable = false)
    @Builder.Default
    private Integer enrolledCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private Teacher classTeacher;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
