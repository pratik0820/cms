package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportManagementResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private Summary summary;
    private List<ReportCategoryResponse> categories;
    private List<ReportResponse> recentReports;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long totalStudents;
        private long totalTeachers;
        private long totalAdmissions;
        private BigDecimal feesCollected;
        private BigDecimal pendingFees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportCategoryResponse {
        private String key;
        private String label;
        private String description;
        private List<ReportTypeResponse> reportTypes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportTypeResponse {
        private String key;
        private String label;
        private String defaultFormat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMeta {
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportListResponse {
        private List<ReportResponse> content;
        private PageMeta page;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateReportResponse {
        private UUID id;
        private String reportName;
        private String category;
        private String reportType;
        private String format;
        private String status;
        private String downloadUrl;
    }
}
