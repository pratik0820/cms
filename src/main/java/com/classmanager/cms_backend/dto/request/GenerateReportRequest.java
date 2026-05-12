package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class GenerateReportRequest {

    @NotBlank(message = "Report category is required")
    private String category;

    @NotBlank(message = "Report type is required")
    private String reportType;

    private String format;

    private UUID branchId;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String standard;

    private String batch;
}
