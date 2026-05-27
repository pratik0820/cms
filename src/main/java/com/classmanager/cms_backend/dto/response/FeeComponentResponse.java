package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class FeeComponentResponse {
    private UUID id;
    private UUID enrolmentId;
    private UUID studentId;
    private String studentName;
    private String rollNo;
    private String className;
    private String batchName;
    
    private String feeType;
    private BigDecimal totalFee;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private LocalDate dueDate;
    private String status; // Paid, Partial, Overdue, Pending
}
