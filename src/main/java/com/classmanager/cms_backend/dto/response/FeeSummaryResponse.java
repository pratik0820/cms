package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FeeSummaryResponse {
    private UUID enrolmentId;
    private String studentName;
    private String rollNo;
    private String className;
    private String batchName;
    private String phone;
    
    private BigDecimal totalFeesYearly;
    private BigDecimal totalPaid;
    private BigDecimal balanceFees;
    private BigDecimal dueFees;

    private List<FeeComponentResponse> feeComponents;
    private List<FeeTransactionResponse> transactions;
}
