package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateBranchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBranchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBranchStatusRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.BranchManagementResponse;
import com.classmanager.cms_backend.dto.response.BranchResponse;
import com.classmanager.cms_backend.dto.response.SuperAdminDashboardResponse;
import com.classmanager.cms_backend.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Branch management APIs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class BranchController extends BaseController {

    private final BranchService branchService;

    @GetMapping("/options")
    @Operation(summary = "Get active branch options")
    public ResponseEntity<ApiResponse<List<SuperAdminDashboardResponse.BranchOption>>> getBranchOptions() {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchOptions()));
    }

    @GetMapping
    @Operation(summary = "Get branch overview summary and paginated branch list")
    public ResponseEntity<ApiResponse<BranchManagementResponse>> getBranches(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranches(search, isActive, page, size)));
    }

    @GetMapping("/{branchId}")
    @Operation(summary = "Get a single branch")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranch(@PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranch(branchId)));
    }

    @PostMapping
    @Operation(summary = "Create a new branch")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(branchService.createBranch(request, currentUserId()), "Branch created successfully"));
    }

    @PutMapping("/{branchId}")
    @Operation(summary = "Update an existing branch")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(@PathVariable UUID branchId, @Valid @RequestBody UpdateBranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(branchService.updateBranch(branchId, request, currentUserId()), "Branch updated successfully"));
    }

    @PatchMapping("/{branchId}/status")
    @Operation(summary = "Activate or deactivate a branch")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranchStatus(@PathVariable UUID branchId,
                                                                          @Valid @RequestBody UpdateBranchStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(branchService.updateBranchStatus(branchId, request, currentUserId()), "Branch status updated successfully"));
    }

    @DeleteMapping("/{branchId}")
    @Operation(summary = "Soft delete a branch")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable UUID branchId) {
        branchService.deleteBranch(branchId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Branch deleted successfully"));
    }
}
