package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class StudentFeeSummaryResponse {
    private UUID enrolmentId;
    private UUID studentId;
    private String studentName;
    private String rollNo;
    private String className;
    private String batchName;
    
    private BigDecimal totalFee;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    
    private String status;
}
