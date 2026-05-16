package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateEnrolmentRequest;
import com.classmanager.cms_backend.dto.request.UpdateEnrolmentRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.StudentEnrolmentResponse;
import com.classmanager.cms_backend.service.StudentEnrolmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrolments")
@RequiredArgsConstructor
@Tag(name = "Student Enrolments", description = "Enrol students into batches with manual fee entry")
public class StudentEnrolmentController extends BaseController {

    private final StudentEnrolmentService enrolmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Enrol a student into a batch — admin selects subjects and enters fee manually")
    public ResponseEntity<ApiResponse<StudentEnrolmentResponse>> createEnrolment(
            @Valid @RequestBody CreateEnrolmentRequest request) {
        StudentEnrolmentResponse response = enrolmentService.createEnrolment(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Student enrolled successfully"));
    }

    @GetMapping("/{enrolmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get enrolment details")
    public ResponseEntity<ApiResponse<StudentEnrolmentResponse>> getEnrolment(
            @PathVariable UUID enrolmentId) {
        return ResponseEntity.ok(ApiResponse.success(enrolmentService.getEnrolment(enrolmentId)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all enrolments for a student (all batches and years)")
    public ResponseEntity<ApiResponse<List<StudentEnrolmentResponse>>> getByStudent(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(enrolmentService.getEnrolmentsByStudent(studentId)));
    }

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all enrolments in a batch (paginated)")
    public ResponseEntity<ApiResponse<Page<StudentEnrolmentResponse>>> getByBatch(
            @PathVariable UUID batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StudentEnrolmentResponse> result = enrolmentService.getEnrolmentsByBatch(
                batchId, PageRequest.of(page, size, Sort.by("student.name")));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "List enrolments by branch and academic year (paginated)")
    public ResponseEntity<ApiResponse<Page<StudentEnrolmentResponse>>> getByBranch(
            @PathVariable UUID branchId,
            @RequestParam String academicYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StudentEnrolmentResponse> result = enrolmentService.getEnrolmentsByBranchAndYear(
                branchId, academicYear, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{enrolmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update enrolment — change subjects, fee, payment plan, instalments or status")
    public ResponseEntity<ApiResponse<StudentEnrolmentResponse>> updateEnrolment(
            @PathVariable UUID enrolmentId,
            @Valid @RequestBody UpdateEnrolmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                enrolmentService.updateEnrolment(enrolmentId, request), "Enrolment updated"));
    }

    @DeleteMapping("/{enrolmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Soft-delete an enrolment")
    public ResponseEntity<ApiResponse<Void>> deleteEnrolment(@PathVariable UUID enrolmentId) {
        enrolmentService.deleteEnrolment(enrolmentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Enrolment deleted"));
    }
}
