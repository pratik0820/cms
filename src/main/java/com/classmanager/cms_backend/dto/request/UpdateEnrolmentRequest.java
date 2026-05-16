package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.EnrolmentStatus;
import com.classmanager.cms_backend.enums.FeePaymentPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Partial update for an existing enrolment.
 * All fields are optional — only non-null fields are applied.
 */
@Data
public class UpdateEnrolmentRequest {

    private UUID subjectGroupId;

    /** Replace the full subject list. */
    private List<UUID> subjectIds;

    @DecimalMin(value = "0.0", message = "Fee cannot be negative")
    private BigDecimal agreedTotalFee;

    private FeePaymentPlan paymentPlan;

    /** Replace the full instalment schedule. */
    @Valid
    private List<CreateEnrolmentRequest.InstalmentRequest> instalments;

    private EnrolmentStatus status;

    private String notes;

    private LocalDate enrolmentDate;
}
