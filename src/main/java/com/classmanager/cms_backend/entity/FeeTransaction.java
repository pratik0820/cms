package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fee_transactions", indexes = {
        @Index(name = "idx_fee_transactions_enrolment", columnList = "enrolment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolment_id", nullable = false)
    private StudentEnrolment enrolment;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "payment_mode", nullable = false, length = 50)
    private String paymentMode;

    @Column(name = "reference_no", length = 255)
    private String referenceNo;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "total_selected_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSelectedAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "late_fee_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal lateFeeAmount = BigDecimal.ZERO;

    @Column(name = "total_payable_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPayableAmount;

    @Column(name = "amount_received", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountReceived;

    @Column(name = "receipt_url", length = 1000)
    private String receiptUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FeeTransactionDetail> details = new ArrayList<>();
}
