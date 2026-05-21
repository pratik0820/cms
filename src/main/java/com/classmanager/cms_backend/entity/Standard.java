package com.classmanager.cms_backend.entity;

import com.classmanager.cms_backend.enums.BoardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "standards", indexes = {
        @Index(name = "idx_standards_code", columnList = "code", unique = true),
        @Index(name = "idx_standards_name_board", columnList = "name, board")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Standard extends BaseEntity {

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "board", nullable = false, length = 20)
    private BoardType board;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
