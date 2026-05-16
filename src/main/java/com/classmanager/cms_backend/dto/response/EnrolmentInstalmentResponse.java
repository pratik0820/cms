package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class EnrolmentInstalmentResponse {
    private UUID id;
    private Integer instalmentNumber;
    private String label;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Boolean isPostDatedCheque;
    private Boolean isPaid;
    private LocalDate paidDate;
}
