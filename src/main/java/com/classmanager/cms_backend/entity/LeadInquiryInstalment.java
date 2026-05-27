package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lead_inquiry_installments", indexes = {
        @Index(name = "idx_lead_inquiry_installments_lead", columnList = "lead_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadInquiryInstalment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private LeadInquiry lead;

    @Column(name = "instalment_number", nullable = false)
    private Integer instalmentNumber;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date")
    private LocalDate dueDate;
}
