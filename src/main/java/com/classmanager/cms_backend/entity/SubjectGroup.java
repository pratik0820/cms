package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A SubjectGroup is a named bundle of subjects within a {@link Course}.
 *
 * It represents the selectable "subject set" shown to the admin when
 * enrolling a student.  Examples:
 *
 *   Course: "8th CBSE"
 *     Group 1 – "Chanakya (All Subjects)"  → Maths, Science, English, Language, SST
 *     Group 2 – "Drona"                    → Maths, Science, English, SST
 *     Group 3 – "Vyasa"                    → Maths, Science, English
 *     Group 4 – "Arjuna"                   → Maths, Science
 *
 *   Course: "11th-12th HSC + JEE/NEET"
 *     Group 1 – "PCM (3 subjects)"         → Physics, Chemistry, Maths
 *     Group 2 – "PCM (4 subjects)"         → Physics, Chemistry, Maths + Biology
 *
 * {@code isExtraSubjectAllowed} signals that the admin can add extra subjects
 * on top of this group's default subjects.
 */
@Entity
@Table(name = "subject_groups", indexes = {
        @Index(name = "idx_subject_groups_course", columnList = "course_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "short_name", length = 40)
    private String shortName;

    /** Canonical subject count for fee tier lookup (e.g. 3 or 4 for HSC). */
    @Column(name = "subject_count", nullable = false)
    private Integer subjectCount;

    /** Whether the admin can add extra subjects beyond this group. */
    @Column(name = "is_extra_subject_allowed", nullable = false)
    @Builder.Default
    private Boolean isExtraSubjectAllowed = false;

    /** Maximum extra subjects allowed (0 = unlimited). */
    @Column(name = "max_extra_subjects", nullable = false)
    @Builder.Default
    private Integer maxExtraSubjects = 0;

    /** Subjects included in this group by default. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_group_subjects",
            joinColumns = @JoinColumn(name = "subject_group_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();

    /** Subjects that can be added as extras (empty = any active subject). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_group_extra_subjects",
            joinColumns = @JoinColumn(name = "subject_group_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private List<Subject> allowedExtraSubjects = new ArrayList<>();

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
