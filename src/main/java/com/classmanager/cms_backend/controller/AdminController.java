package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateAdminRequest;
import com.classmanager.cms_backend.dto.request.UpdateAdminRequest;
import com.classmanager.cms_backend.dto.request.UpdateAdminStatusRequest;
import com.classmanager.cms_backend.dto.response.AdminManagementResponse;
import com.classmanager.cms_backend.dto.response.AdminResponse;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/admins")
@RequiredArgsConstructor
@Tag(name = "Admins", description = "Admin management APIs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController extends BaseController {

    private final AdminService adminService;

    @GetMapping
    @Operation(summary = "Get admin management summary and paginated admin list")
    public ResponseEntity<ApiResponse<AdminManagementResponse>> getAdmins(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAdmins(search, isActive, branchId, page, size)));
    }

    @GetMapping("/{adminId}")
    @Operation(summary = "Get a single admin profile")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdmin(@PathVariable UUID adminId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAdmin(adminId)));
    }

    @PostMapping
    @Operation(summary = "Create a new admin account")
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createAdmin(request, currentUserId()), "Admin created successfully"));
    }

    @PutMapping("/{adminId}")
    @Operation(summary = "Update an existing admin profile")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(@PathVariable UUID adminId, @Valid @RequestBody UpdateAdminRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateAdmin(adminId, request, currentUserId()), "Admin updated successfully"));
    }

    @PatchMapping("/{adminId}/status")
    @Operation(summary = "Activate or deactivate an admin account")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdminStatus(@PathVariable UUID adminId,
                                                                        @Valid @RequestBody UpdateAdminStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateAdminStatus(adminId, request, currentUserId()), "Admin status updated successfully"));
    }

    @DeleteMapping("/{adminId}")
    @Operation(summary = "Soft delete an admin account")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable UUID adminId) {
        adminService.deleteAdmin(adminId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Admin deleted successfully"));
    }
}
