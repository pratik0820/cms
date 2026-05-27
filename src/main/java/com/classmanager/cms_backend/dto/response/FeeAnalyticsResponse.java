package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class FeeAnalyticsResponse {
    private BigDecimal totalExpected;
    private BigDecimal totalCollected;
    private BigDecimal totalPending;
    private BigDecimal totalOverdue;
    private int totalStudents;
    private int overdueStudentsCount;

    private List<FeeTypeSummary> feeTypeSummaries;

    @Data
    @Builder
    public static class FeeTypeSummary {
        private String feeType;
        private BigDecimal totalAmount;
    }
}
