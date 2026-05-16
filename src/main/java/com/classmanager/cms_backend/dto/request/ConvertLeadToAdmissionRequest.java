package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.FeePaymentPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ConvertLeadToAdmissionRequest {

    private String studentId;
    private UUID branchId;
    private String standard;
    private String batch;
    private String board;
    private LocalDate admissionDate;

    private UUID batchId;
    private UUID subjectGroupId;
    private List<UUID> subjectIds;

    @DecimalMin(value = "0.0", message = "Agreed fee cannot be negative")
    private BigDecimal agreedTotalFee;

    private FeePaymentPlan paymentPlan;

    @Valid
    private List<CreateEnrolmentRequest.InstalmentRequest> instalments;

    private String notes;
}
