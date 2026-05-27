package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CollectFeeRequest {

    @NotNull
    private LocalDate paymentDate;
    
    @NotNull
    private String paymentMode;

    private String referenceNo;
    private String remarks;
    
    private BigDecimal discountAmount;
    private BigDecimal lateFeeAmount;
    
    @NotNull
    private BigDecimal amountReceived;

    private List<AllocationRequest> allocations;
    private List<NewChargeRequest> newCharges;

    @Data
    public static class AllocationRequest {
        @NotNull
        private UUID instalmentId;
        @NotNull
        private BigDecimal amount;
    }

    @Data
    public static class NewChargeRequest {
        @NotNull
        private String label;
        @NotNull
        private BigDecimal amount;
    }
}
