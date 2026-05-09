package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "hourly_rate", nullable = false)
    @Builder.Default
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
