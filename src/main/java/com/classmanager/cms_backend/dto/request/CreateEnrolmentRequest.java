package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.FeePaymentPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request to enrol a student into a batch.
 *
 * The admin:
 *   1. Selects a batch (required).
 *   2. Optionally selects a subject group as a starting point.
 *   3. Selects the actual subjects the student will take (required, at least one).
 *   4. Enters the total agreed fee manually — no backend calculation.
 *   5. Selects a payment plan.
 *   6. Enters the instalment schedule manually.
 */
@Data
public class CreateEnrolmentRequest {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Batch ID is required")
    private UUID batchId;

    /**
     * Optional: subject group selected as a starting point.
     * The frontend can pre-fill subjects from this group, but the admin
     * can freely add or remove subjects before saving.
     */
    private UUID subjectGroupId;

    /**
     * Actual subjects the student is enrolled for.
     * Must contain at least one subject.
     */
    @NotEmpty(message = "At least one subject must be selected")
    private List<UUID> subjectIds;

    /**
     * Total fee agreed by the admin. Entered manually — no backend calculation.
     */
    @NotNull(message = "Agreed total fee is required")
    @DecimalMin(value = "0.0", message = "Fee cannot be negative")
    private BigDecimal agreedTotalFee;

    @NotNull(message = "Payment plan is required")
    private FeePaymentPlan paymentPlan;

    @NotNull(message = "Enrolment date is required")
    private LocalDate enrolmentDate;

    /**
     * Instalment schedule entered by the admin.
     * For REGULAR (single payment) this can be a single-item list or empty.
     */
    @Valid
    private List<InstalmentRequest> instalments;

    /** Optional notes (e.g. "Sibling discount applied"). */
    private String notes;

    @Data
    public static class InstalmentRequest {

        @NotNull(message = "Instalment number is required")
        private Integer instalmentNumber;

        @NotNull(message = "Instalment label is required")
        private String label;

        @NotNull(message = "Instalment amount is required")
        @DecimalMin(value = "0.0", message = "Instalment amount cannot be negative")
        private BigDecimal amount;

        private LocalDate dueDate;

        private Boolean isPostDatedCheque = false;
    }
}
