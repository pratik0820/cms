package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.SyllabusDashboardResponse;
import com.classmanager.cms_backend.service.SyllabusDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic/syllabus/dashboard")
@RequiredArgsConstructor
@Tag(name = "Syllabus Dashboard", description = "APIs for Syllabus Dashboard Analytics")
public class SyllabusDashboardController extends BaseController {

    private final SyllabusDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get syllabus completion dashboard statistics")
    public ResponseEntity<ApiResponse<SyllabusDashboardResponse>> getDashboardStats(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String standard,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getDashboardStats(branchId, standard, subjectId, teacherId, status, startDate, endDate)
        ));
    }
}
