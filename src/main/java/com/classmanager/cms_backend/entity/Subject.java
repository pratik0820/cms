package com.classmanager.cms_backend.entity;

import com.classmanager.cms_backend.enums.SubjectCode;
import jakarta.persistence.*;
import lombok.*;

/**
 * Master subject catalogue.
 * {@code code} is the canonical enum value; {@code displayName} is the
 * human-readable label shown in the UI (admin can rename without touching code).
 */
@Entity
@Table(name = "subjects", indexes = {
        @Index(name = "idx_subjects_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 40, unique = true)
    private SubjectCode code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "short_name", length = 20)
    private String shortName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
