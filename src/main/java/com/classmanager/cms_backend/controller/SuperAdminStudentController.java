package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateStudentRequest;
import com.classmanager.cms_backend.dto.request.UpdateStudentRequest;
import com.classmanager.cms_backend.dto.request.UpdateStudentStatusRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.StudentManagementResponse;
import com.classmanager.cms_backend.dto.response.StudentResponse;
import com.classmanager.cms_backend.service.StudentManagementService;
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
@RequestMapping("/api/super-admin/students")
@RequiredArgsConstructor
@Tag(name = "Super Admin - Student Management", description = "Student management APIs for the super admin phase")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminStudentController extends BaseController {

    private final StudentManagementService studentManagementService;

    @GetMapping
    @Operation(summary = "Get student management summary and paginated student list")
    public ResponseEntity<ApiResponse<StudentManagementResponse>> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String standard,
            @RequestParam(required = false) String batch,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        StudentManagementResponse response = studentManagementService.getStudents(search, isActive, branchId, standard, batch, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{studentId}")
    @Operation(summary = "Get a single student profile")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(studentManagementService.getStudent(studentId)));
    }

    @PostMapping
    @Operation(summary = "Create a new student account")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse response = studentManagementService.createStudent(request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Student created successfully"));
    }

    @PutMapping("/{studentId}")
    @Operation(summary = "Update an existing student profile")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable UUID studentId,
            @Valid @RequestBody UpdateStudentRequest request) {

        StudentResponse response = studentManagementService.updateStudent(studentId, request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Student updated successfully"));
    }

    @PatchMapping("/{studentId}/status")
    @Operation(summary = "Activate or deactivate a student account")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudentStatus(
            @PathVariable UUID studentId,
            @Valid @RequestBody UpdateStudentStatusRequest request) {

        StudentResponse response = studentManagementService.updateStudentStatus(studentId, request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Student status updated successfully"));
    }

    @DeleteMapping("/{studentId}")
    @Operation(summary = "Soft delete a student account")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID studentId) {
        studentManagementService.deleteStudent(studentId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"));
    }
}
