package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateTeacherRequest;
import com.classmanager.cms_backend.dto.request.UpdateTeacherRequest;
import com.classmanager.cms_backend.dto.request.UpdateTeacherStatusRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.TeacherManagementResponse;
import com.classmanager.cms_backend.dto.response.TeacherResponse;
import com.classmanager.cms_backend.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Teacher management APIs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TeacherController extends BaseController {

    private final TeacherService teacherService;

    @GetMapping
    @Operation(summary = "Get teacher management summary and paginated teacher list")
    public ResponseEntity<ApiResponse<TeacherManagementResponse>> getTeachers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                teacherService.getTeachers(search, isActive, branchId, subject, subjectId, courseId, batchId, page, size)));
    }

    @GetMapping("/{teacherId}")
    @Operation(summary = "Get a single teacher profile")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getTeacher(teacherId)));
    }

    @PostMapping
    @Operation(summary = "Create a new teacher account")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.createTeacher(request, currentUserId()), "Teacher created successfully"));
    }

    @PutMapping("/{teacherId}")
    @Operation(summary = "Update an existing teacher profile")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(@PathVariable UUID teacherId, @Valid @RequestBody UpdateTeacherRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.updateTeacher(teacherId, request, currentUserId()), "Teacher updated successfully"));
    }

    @PatchMapping("/{teacherId}/status")
    @Operation(summary = "Activate or deactivate a teacher account")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacherStatus(@PathVariable UUID teacherId,
                                                                            @Valid @RequestBody UpdateTeacherStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.updateTeacherStatus(teacherId, request, currentUserId()), "Teacher status updated successfully"));
    }

    @DeleteMapping("/{teacherId}")
    @Operation(summary = "Soft delete a teacher account")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable UUID teacherId) {
        teacherService.deleteTeacher(teacherId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher deleted successfully"));
    }
}
