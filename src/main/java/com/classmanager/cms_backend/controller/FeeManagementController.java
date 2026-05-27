package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CollectFeeRequest;
import com.classmanager.cms_backend.dto.response.FeeAnalyticsResponse;
import com.classmanager.cms_backend.dto.response.FeeComponentResponse;
import com.classmanager.cms_backend.dto.response.FeeSummaryResponse;
import com.classmanager.cms_backend.dto.response.StudentFeeSummaryResponse;
import com.classmanager.cms_backend.service.FeeManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeeManagementController {

    private final FeeManagementService feeManagementService;

    @GetMapping("/super-admin/fees/components")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<FeeComponentResponse>> getSuperAdminFeeComponents(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) UUID classId,
            @RequestParam(required = false) String feeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(feeManagementService.getFeeComponents(branchId, month, classId, feeType, status, search, pageable));
    }

    @GetMapping("/super-admin/fees/students")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<StudentFeeSummaryResponse>> getSuperAdminStudentFees(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(feeManagementService.getStudentFeeSummaries(branchId, search, pageable));
    }

    @GetMapping("/super-admin/fees/enrolments/{enrolmentId}/summary")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<FeeSummaryResponse> getSuperAdminFeeSummary(@PathVariable UUID enrolmentId) {
        return ResponseEntity.ok(feeManagementService.getFeeSummaryByEnrolment(enrolmentId));
    }

    @PostMapping("/super-admin/fees/enrolments/{enrolmentId}/collect")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<FeeSummaryResponse> collectSuperAdminFees(
            @PathVariable UUID enrolmentId,
            @Valid @RequestBody CollectFeeRequest request) {
        return ResponseEntity.ok(feeManagementService.collectFees(enrolmentId, request));
    }

    @GetMapping("/super-admin/fees/analytics")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<FeeAnalyticsResponse> getSuperAdminFeeAnalytics(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(feeManagementService.getFeeAnalytics(branchId, month));
    }

    // Admin Endpoints
    @GetMapping("/admin/fees/components")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<FeeComponentResponse>> getAdminFeeComponents(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) UUID classId,
            @RequestParam(required = false) String feeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // In a real implementation, branchId should be derived from the logged-in admin's profile
        UUID adminBranchId = null; 
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(feeManagementService.getFeeComponents(adminBranchId, month, classId, feeType, status, search, pageable));
    }

    @GetMapping("/admin/fees/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<StudentFeeSummaryResponse>> getAdminStudentFees(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        UUID adminBranchId = null; // In real implementation, derive from logged-in admin
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(feeManagementService.getStudentFeeSummaries(adminBranchId, search, pageable));
    }

    @GetMapping("/admin/fees/enrolments/{enrolmentId}/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<FeeSummaryResponse> getAdminFeeSummary(@PathVariable UUID enrolmentId) {
        return ResponseEntity.ok(feeManagementService.getFeeSummaryByEnrolment(enrolmentId));
    }

    @PostMapping("/admin/fees/enrolments/{enrolmentId}/collect")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<FeeSummaryResponse> collectAdminFees(
            @PathVariable UUID enrolmentId,
            @Valid @RequestBody CollectFeeRequest request) {
        return ResponseEntity.ok(feeManagementService.collectFees(enrolmentId, request));
    }

    @GetMapping("/admin/fees/analytics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<FeeAnalyticsResponse> getAdminFeeAnalytics(
            @RequestParam(required = false) String month) {
        UUID adminBranchId = null; // In real implementation, derive from logged-in admin
        return ResponseEntity.ok(feeManagementService.getFeeAnalytics(adminBranchId, month));
    }
}
