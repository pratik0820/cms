package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One instalment row in a student's fee payment schedule.
 * All amounts are entered manually by the admin at enrolment time.
 */
@Entity
@Table(name = "enrolment_instalments", indexes = {
        @Index(name = "idx_enrolment_instalments_enrolment", columnList = "enrolment_id"),
        @Index(name = "idx_enrolment_instalments_due_date",  columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrolmentInstalment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolment_id", nullable = false)
    private StudentEnrolment enrolment;

    @Column(name = "instalment_number", nullable = false)
    private Integer instalmentNumber;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    /** Amount entered by the admin. */
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "is_post_dated_cheque", nullable = false)
    @Builder.Default
    private Boolean isPostDatedCheque = false;

    /** Updated by the fee collection module when payment is recorded. */
    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = false;

    @Column(name = "paid_date")
    private LocalDate paidDate;
}
