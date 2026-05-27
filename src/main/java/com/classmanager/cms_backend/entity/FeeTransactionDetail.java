package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_transaction_details", indexes = {
        @Index(name = "idx_fee_transaction_details_tx", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeTransactionDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private FeeTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instalment_id", nullable = false)
    private EnrolmentInstalment instalment;

    @Column(name = "allocated_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal allocatedAmount;
}
