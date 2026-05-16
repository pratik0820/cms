package com.classmanager.cms_backend.dto.response;

import com.classmanager.cms_backend.enums.EnrolmentStatus;
import com.classmanager.cms_backend.enums.FeePaymentPlan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StudentEnrolmentResponse {

    private UUID id;

    // Student
    private UUID studentId;
    private String studentName;
    private String studentLoginId;

    // Batch
    private UUID batchId;
    private String batchName;
    private String academicYear;

    // Course (from batch)
    private UUID courseId;
    private String courseName;
    private String courseCode;

    // Subject selection
    private UUID subjectGroupId;
    private String subjectGroupName;
    private List<SubjectResponse> subjects;

    // Fee (manually entered by admin)
    private BigDecimal agreedTotalFee;
    private FeePaymentPlan paymentPlan;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;

    // Schedule (manually entered by admin)
    private List<EnrolmentInstalmentResponse> instalments;

    // Meta
    private LocalDate enrolmentDate;
    private EnrolmentStatus status;
    private String notes;
}
