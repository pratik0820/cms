package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private UUID id;
    private String reportName;
    private String category;
    private String categoryLabel;
    private String reportType;
    private String reportTypeLabel;
    private String description;
    private String generatedBy;
    private LocalDateTime generatedOn;
    private String format;
    private String status;
    private UUID branchId;
    private String branchName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String downloadUrl;
}
