package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.GenerateReportRequest;
import com.classmanager.cms_backend.dto.response.ReportFile;
import com.classmanager.cms_backend.dto.response.ReportManagementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SuperAdminReportService superAdminReportService;

    public ReportManagementResponse getSummary(LocalDate fromDate, LocalDate toDate, UUID branchId) {
        return superAdminReportService.getSummary(fromDate, toDate, branchId);
    }

    public List<ReportManagementResponse.ReportCategoryResponse> getCategories() {
        return superAdminReportService.getCategories();
    }

    public ReportManagementResponse.ReportListResponse searchReports(String category, String reportType, String format,
                                                                     UUID branchId, LocalDate fromDate, LocalDate toDate,
                                                                     String search, int page, int size) {
        return superAdminReportService.searchReports(category, reportType, format, branchId, fromDate, toDate, search, page, size);
    }

    public ReportManagementResponse.GenerateReportResponse generateReport(GenerateReportRequest request, UUID generatedByUserId) {
        return superAdminReportService.generateReport(request, generatedByUserId);
    }

    public ReportFile downloadReport(UUID reportId) {
        return superAdminReportService.downloadReport(reportId);
    }

    public void deleteReport(UUID reportId) {
        superAdminReportService.deleteReport(reportId);
    }
}
