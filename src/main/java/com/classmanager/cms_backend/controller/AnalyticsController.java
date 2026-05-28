package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.SuperAdminAnalyticsResponse;
import com.classmanager.cms_backend.service.AnalyticsService;
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
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get the analytics data")
    public ResponseEntity<ApiResponse<SuperAdminAnalyticsResponse>> getAnalytics(
            @RequestParam(required = false, defaultValue = "overview") String tab,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAnalytics(tab, fromDate, toDate, branchId)));
    }
}
