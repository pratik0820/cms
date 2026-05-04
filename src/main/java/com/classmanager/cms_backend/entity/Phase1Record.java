package com.classmanager.cms_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "phase1_records", indexes = {
        @Index(name = "idx_phase1_tenant_module", columnList = "tenant_id, module"),
        @Index(name = "idx_phase1_branch_module", columnList = "branch_id, module"),
        @Index(name = "idx_phase1_student_module", columnList = "student_id, module"),
        @Index(name = "idx_phase1_teacher_module", columnList = "teacher_id, module")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phase1Record extends BaseEntity {

    @Column(name = "module", nullable = false, length = 60)
    private String module;

    @Column(name = "type", length = 80)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "status", length = 60)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "teacher_id")
    private UUID teacherId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "source", length = 120)
    private String source;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "pending_amount", precision = 12, scale = 2)
    private BigDecimal pendingAmount;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "max_score")
    private BigDecimal maxScore;

    @Column(name = "percentage")
    private BigDecimal percentage;

    @Lob
    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;
}
