package com.classmanager.cms_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "admin_profiles", indexes = {
        @Index(name = "idx_admin_profiles_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_admin_profiles_joining_date", columnList = "joining_date"),
        @Index(name = "idx_admin_profiles_all_branches_access", columnList = "all_branches_access")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 30)
    private String gender;

    @Column(name = "role_title", nullable = false, length = 80)
    private String roleTitle;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "access_level", nullable = false, length = 80)
    private String accessLevel;

    @Column(name = "address", length = 2000)
    private String address;

    @Column(name = "all_branches_access", nullable = false)
    @Builder.Default
    private Boolean allBranchesAccess = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;
}
