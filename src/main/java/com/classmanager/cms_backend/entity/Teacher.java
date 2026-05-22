package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers", indexes = {
        @Index(name = "idx_teachers_branch", columnList = "branch_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Teacher extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @ElementCollection
    @CollectionTable(name = "teacher_subjects", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject", nullable = false)
    @Builder.Default
    private List<String> subjects = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teacher_subject_assignments",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private List<Subject> catalogSubjects = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teacher_course_assignments",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teacher_batch_assignments",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "batch_id")
    )
    @Builder.Default
    private List<Batch> batches = new ArrayList<>();

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "salary_type")
    private String salaryType;

    @Column(name = "hourly_rate", nullable = false)
    @Builder.Default
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "address", length = 2000)
    private String address;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;
}
