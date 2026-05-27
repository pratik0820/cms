package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeeTransactionResponse {
    private UUID id;
    private LocalDateTime transactionDate;
    private String paymentMode;
    private String referenceNo;
    private String remarks;
    private BigDecimal amountReceived;
    private BigDecimal discountAmount;
    private BigDecimal lateFeeAmount;
    private String recordedBy;
}
