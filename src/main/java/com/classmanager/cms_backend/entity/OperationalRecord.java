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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "operational_records", indexes = {
        @Index(name = "idx_operational_records_module", columnList = "module"),
        @Index(name = "idx_operational_records_branch_module", columnList = "branch_id, module"),
        @Index(name = "idx_operational_records_event_date", columnList = "event_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalRecord extends BaseEntity {

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

    @Lob
    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;
}
