package com.classmanager.cms_backend.entity;


import com.classmanager.cms_backend.enums.BoardType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_students_tenant_branch", columnList = "tenant_id, branch_id"),
        @Index(name = "idx_students_login_id",      columnList = "login_id",            unique = true)
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender")
    private String gender;

    /** NULL = admission not yet finalised. Required before finalise(). */
    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "parent_phone")
    private String parentPhone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "standard")
    private String standard;

    @Enumerated(EnumType.STRING)
    @Column(name = "board")
    private BoardType board;

    /** Auto-generated unique login ID e.g. STU-A3X9KL */
    @Column(name = "login_id", unique = true)
    private String loginId;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "is_admission_final", nullable = false)
    @Builder.Default
    private Boolean isAdmissionFinal = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
