package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.SuperAdminDashboardResponse;
import com.classmanager.cms_backend.service.SuperAdminDashboardService;
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
@Tag(name = "Super Admin Dashboard", description = "Dashboard APIs for the super admin phase")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService superAdminDashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get the super admin dashboard data")
    public ResponseEntity<ApiResponse<SuperAdminDashboardResponse>> getDashboard(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        SuperAdminDashboardResponse response = superAdminDashboardService.getDashboard(fromDate, toDate, branchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
