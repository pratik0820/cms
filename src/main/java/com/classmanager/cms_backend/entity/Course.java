package com.classmanager.cms_backend.entity;

import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A Course defines a specific academic offering at a particular standard
 * and board combination.
 *
 * Examples:
 *   "8th CBSE"          (category=BOARD_REGULAR, board=CBSE, standard="8")
 *   "11th-12th HSC+JEE" (category=COMBINED,      board=null, standard="11-12")
 *   "Foundation 7th"    (category=FOUNDATION,     board=null, standard="7")
 *
 * Courses are global master data (not per-branch).
 */
@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_board_standard", columnList = "board, standard"),
        @Index(name = "idx_courses_category",       columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 40, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private CourseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "board", length = 10)
    private BoardType board;

    /** "8", "9", "10", "11", "12", "11-12", "7", etc. */
    @Column(name = "standard", nullable = false, length = 10)
    private String standard;

    @Column(name = "academic_year", length = 10)
    private String academicYear;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<SubjectGroup> subjectGroups = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
