package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Master subject catalogue.
 * {@code code} is a stable catalog identifier; {@code displayName} is the
 * human-readable label shown in the UI.
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

    @Column(name = "display_code", length = 20, unique = true)
    private String displayCode;

    @Column(name = "code", nullable = false, length = 40, unique = true)
    private String code;

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
