package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.GenerateReportRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.ReportFile;
import com.classmanager.cms_backend.dto.response.ReportManagementResponse;
import com.classmanager.cms_backend.service.SuperAdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/reports")
@RequiredArgsConstructor
@Tag(name = "Super Admin - Reports", description = "Report APIs for the super admin phase")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminReportController extends BaseController {

    private final SuperAdminReportService superAdminReportService;

    @GetMapping("/summary")
    @Operation(summary = "Get reports dashboard summary")
    public ResponseEntity<ApiResponse<ReportManagementResponse>> getSummary(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return ResponseEntity.ok(ApiResponse.success(superAdminReportService.getSummary(fromDate, toDate, branchId)));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get report categories and available report types")
    public ResponseEntity<ApiResponse<List<ReportManagementResponse.ReportCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(superAdminReportService.getCategories()));
    }

    @GetMapping
    @Operation(summary = "Get generated reports list")
    public ResponseEntity<ApiResponse<ReportManagementResponse.ReportListResponse>> getReports(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(superAdminReportService.searchReports(
                category, reportType, format, branchId, fromDate, toDate, search, page, size
        )));
    }

    @PostMapping("/export")
    @Operation(summary = "Generate a report and return its download metadata")
    public ResponseEntity<ApiResponse<ReportManagementResponse.GenerateReportResponse>> generateReport(
            @Valid @RequestBody GenerateReportRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                superAdminReportService.generateReport(request, currentUserId()),
                "Report generated successfully"
        ));
    }

    @GetMapping("/{reportId}/download")
    @Operation(summary = "Download a generated report")
    public ResponseEntity<ByteArrayResource> downloadReport(@PathVariable UUID reportId) {
        ReportFile reportFile = superAdminReportService.downloadReport(reportId);
        ByteArrayResource resource = new ByteArrayResource(reportFile.getContent());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(reportFile.getContentType()))
                .contentLength(reportFile.getContent().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(reportFile.getFilename())
                        .build()
                        .toString())
                .body(resource);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete a generated report record")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable UUID reportId) {
        superAdminReportService.deleteReport(reportId);
        return ResponseEntity.ok(ApiResponse.success(null, "Report deleted successfully"));
    }
}
