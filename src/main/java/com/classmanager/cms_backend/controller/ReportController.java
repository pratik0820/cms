package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.GenerateReportRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.ReportFile;
import com.classmanager.cms_backend.dto.response.ReportManagementResponse;
import com.classmanager.cms_backend.service.ReportService;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report APIs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ReportController extends BaseController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Get reports dashboard summary")
    public ResponseEntity<ApiResponse<ReportManagementResponse>> getSummary(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSummary(fromDate, toDate, branchId)));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get report categories and available report types")
    public ResponseEntity<ApiResponse<List<ReportManagementResponse.ReportCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getCategories()));
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
        return ResponseEntity.ok(ApiResponse.success(reportService.searchReports(category, reportType, format, branchId, fromDate, toDate, search, page, size)));
    }

    @PostMapping("/export")
    @Operation(summary = "Generate a report and return its download metadata")
    public ResponseEntity<ApiResponse<ReportManagementResponse.GenerateReportResponse>> generateReport(
            @Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generateReport(request, currentUserId()), "Report generated successfully"));
    }

    @GetMapping("/{reportId}/download")
    @Operation(summary = "Download a generated report")
    public ResponseEntity<ByteArrayResource> downloadReport(@PathVariable UUID reportId) {
        ReportFile reportFile = reportService.downloadReport(reportId);
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
        reportService.deleteReport(reportId);
        return ResponseEntity.ok(ApiResponse.success(null, "Report deleted successfully"));
    }
}
