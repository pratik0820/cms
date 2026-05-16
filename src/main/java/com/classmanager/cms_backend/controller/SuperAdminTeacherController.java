package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateTeacherRequest;
import com.classmanager.cms_backend.dto.request.UpdateTeacherRequest;
import com.classmanager.cms_backend.dto.request.UpdateTeacherStatusRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.TeacherManagementResponse;
import com.classmanager.cms_backend.dto.response.TeacherResponse;
import com.classmanager.cms_backend.service.SuperAdminTeacherService;
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
@RequestMapping("/api/super-admin/teachers")
@RequiredArgsConstructor
@Tag(name = "Super Admin - Teacher Management", description = "Teacher management APIs for the super admin phase")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminTeacherController extends BaseController {

    private final SuperAdminTeacherService superAdminTeacherService;

    @GetMapping
    @Operation(summary = "Get teacher management summary and paginated teacher list")
    public ResponseEntity<ApiResponse<TeacherManagementResponse>> getTeachers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        TeacherManagementResponse response = superAdminTeacherService.getTeachers(
                search, isActive, branchId, subject, subjectId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{teacherId}")
    @Operation(summary = "Get a single teacher profile")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(ApiResponse.success(superAdminTeacherService.getTeacher(teacherId)));
    }

    @PostMapping
    @Operation(summary = "Create a new teacher account")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        TeacherResponse response = superAdminTeacherService.createTeacher(request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Teacher created successfully"));
    }

    @PutMapping("/{teacherId}")
    @Operation(summary = "Update an existing teacher profile")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @PathVariable UUID teacherId,
            @Valid @RequestBody UpdateTeacherRequest request) {

        TeacherResponse response = superAdminTeacherService.updateTeacher(teacherId, request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Teacher updated successfully"));
    }

    @PatchMapping("/{teacherId}/status")
    @Operation(summary = "Activate or deactivate a teacher account")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacherStatus(
            @PathVariable UUID teacherId,
            @Valid @RequestBody UpdateTeacherStatusRequest request) {

        TeacherResponse response = superAdminTeacherService.updateTeacherStatus(teacherId, request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Teacher status updated successfully"));
    }

    @DeleteMapping("/{teacherId}")
    @Operation(summary = "Soft delete a teacher account")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable UUID teacherId) {
        superAdminTeacherService.deleteTeacher(teacherId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher deleted successfully"));
    }
}
