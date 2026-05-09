package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateAdminRequest;
import com.classmanager.cms_backend.dto.request.UpdateAdminRequest;
import com.classmanager.cms_backend.dto.request.UpdateAdminStatusRequest;
import com.classmanager.cms_backend.dto.response.AdminManagementResponse;
import com.classmanager.cms_backend.dto.response.AdminResponse;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.service.SuperAdminAdminService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/admins")
@RequiredArgsConstructor
@Tag(name = "Super Admin - Admin Management", description = "Admin management APIs for the super admin phase")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminAdminController extends BaseController {

    private final SuperAdminAdminService superAdminAdminService;

    @GetMapping
    @Operation(summary = "Get admin management summary and paginated admin list")
    public ResponseEntity<ApiResponse<AdminManagementResponse>> getAdmins(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AdminManagementResponse response = superAdminAdminService.getAdmins(search, isActive, branchId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{adminId}")
    @Operation(summary = "Get a single admin profile")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdmin(@PathVariable UUID adminId) {
        return ResponseEntity.ok(ApiResponse.success(superAdminAdminService.getAdmin(adminId)));
    }

    @PostMapping
    @Operation(summary = "Create a new admin account")
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        AdminResponse response = superAdminAdminService.createAdmin(request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Admin created successfully"));
    }

    @PutMapping("/{adminId}")
    @Operation(summary = "Update an existing admin profile")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(
            @PathVariable UUID adminId,
            @Valid @RequestBody UpdateAdminRequest request) {

        AdminResponse response = superAdminAdminService.updateAdmin(adminId, request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Admin updated successfully"));
    }

    @PatchMapping("/{adminId}/status")
    @Operation(summary = "Activate or deactivate an admin account")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdminStatus(
            @PathVariable UUID adminId,
            @Valid @RequestBody UpdateAdminStatusRequest request) {

        AdminResponse response = superAdminAdminService.updateAdminStatus(adminId, request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Admin status updated successfully"));
    }

    @DeleteMapping("/{adminId}")
    @Operation(summary = "Soft delete an admin account")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable UUID adminId) {
        superAdminAdminService.deleteAdmin(adminId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Admin deleted successfully"));
    }
}
