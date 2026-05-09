package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateBranchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBranchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBranchStatusRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.BranchManagementResponse;
import com.classmanager.cms_backend.dto.response.BranchResponse;
import com.classmanager.cms_backend.dto.response.SuperAdminDashboardResponse;
import com.classmanager.cms_backend.service.SuperAdminBranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/branches")
@RequiredArgsConstructor
@Tag(name = "Super Admin - Branch Management", description = "Branch management APIs for the super admin phase")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminBranchController extends BaseController {

    private final SuperAdminBranchService superAdminBranchService;

    @GetMapping("/options")
    @Operation(summary = "Get active branch options for filters and selectors")
    public ResponseEntity<ApiResponse<List<SuperAdminDashboardResponse.BranchOption>>> getBranchOptions() {
        return ResponseEntity.ok(ApiResponse.success(superAdminBranchService.getBranchOptions()));
    }

    @GetMapping
    @Operation(summary = "Get branch overview summary and paginated branch list")
    public ResponseEntity<ApiResponse<BranchManagementResponse>> getBranches(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        BranchManagementResponse response = superAdminBranchService.getBranches(search, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{branchId}")
    @Operation(summary = "Get a single branch")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranch(@PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(superAdminBranchService.getBranch(branchId)));
    }

    @PostMapping
    @Operation(summary = "Create a new branch")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        BranchResponse response = superAdminBranchService.createBranch(request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Branch created successfully"));
    }

    @PutMapping("/{branchId}")
    @Operation(summary = "Update an existing branch")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable UUID branchId,
            @Valid @RequestBody UpdateBranchRequest request) {

        BranchResponse response = superAdminBranchService.updateBranch(branchId, request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Branch updated successfully"));
    }

    @PatchMapping("/{branchId}/status")
    @Operation(summary = "Activate or deactivate a branch")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranchStatus(
            @PathVariable UUID branchId,
            @Valid @RequestBody UpdateBranchStatusRequest request) {

        BranchResponse response = superAdminBranchService.updateBranchStatus(branchId, request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Branch status updated successfully"));
    }

    @DeleteMapping("/{branchId}")
    @Operation(summary = "Soft delete a branch")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable UUID branchId) {
        superAdminBranchService.deleteBranch(branchId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Branch deleted successfully"));
    }
}
