package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateStudentRequest;
import com.classmanager.cms_backend.dto.request.UpdateStudentRequest;
import com.classmanager.cms_backend.dto.request.UpdateStudentStatusRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.StudentManagementResponse;
import com.classmanager.cms_backend.dto.response.StudentResponse;
import com.classmanager.cms_backend.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student management APIs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class StudentController extends BaseController {

    private final StudentService studentService;

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
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudents(search, isActive, branchId, standard, batch, page, size)));
    }

    @GetMapping("/{studentId}")
    @Operation(summary = "Get a single student profile")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudent(studentId)));
    }

    @PostMapping
    @Operation(summary = "Create a new student account")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(studentService.createStudent(request, currentUserId()), "Student created successfully"));
    }

    @PutMapping("/{studentId}")
    @Operation(summary = "Update an existing student profile")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(@PathVariable UUID studentId, @Valid @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(studentService.updateStudent(studentId, request, currentUserId()), "Student updated successfully"));
    }

    @PatchMapping("/{studentId}/status")
    @Operation(summary = "Activate or deactivate a student account")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudentStatus(@PathVariable UUID studentId,
                                                                            @Valid @RequestBody UpdateStudentStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(studentService.updateStudentStatus(studentId, request, currentUserId()), "Student status updated successfully"));
    }

    @DeleteMapping("/{studentId}")
    @Operation(summary = "Soft delete a student account")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID studentId) {
        studentService.deleteStudent(studentId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"));
    }
}
