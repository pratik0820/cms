package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateEnrolmentRequest;
import com.classmanager.cms_backend.dto.request.UpdateEnrolmentRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.StudentEnrolmentResponse;
import com.classmanager.cms_backend.service.EnrolmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrolments")
@RequiredArgsConstructor
@Tag(name = "Enrolments", description = "Student enrolment APIs")
public class EnrolmentController {

    private final EnrolmentService enrolmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a student enrolment")
    public ResponseEntity<ApiResponse<StudentEnrolmentResponse>> createEnrolment(@Valid @RequestBody CreateEnrolmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(enrolmentService.createEnrolment(request), "Enrolment created successfully"));
    }

    @GetMapping("/{enrolmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a single enrolment")
    public ResponseEntity<ApiResponse<StudentEnrolmentResponse>> getEnrolment(@PathVariable UUID enrolmentId) {
        return ResponseEntity.ok(ApiResponse.success(enrolmentService.getEnrolment(enrolmentId)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get enrolments by student")
    public ResponseEntity<ApiResponse<List<StudentEnrolmentResponse>>> getEnrolmentsByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(enrolmentService.getEnrolmentsByStudent(studentId)));
    }

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get enrolments by batch")
    public ResponseEntity<ApiResponse<Page<StudentEnrolmentResponse>>> getEnrolmentsByBatch(@PathVariable UUID batchId,
                                                                                              @RequestParam(defaultValue = "0") int page,
                                                                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(enrolmentService.getEnrolmentsByBatch(batchId, PageRequest.of(page, size))));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Get enrolments by branch and academic year")
    public ResponseEntity<ApiResponse<Page<StudentEnrolmentResponse>>> getEnrolmentsByBranchAndYear(@PathVariable UUID branchId,
                                                                                                      @RequestParam String academicYear,
                                                                                                      @RequestParam(defaultValue = "0") int page,
                                                                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(enrolmentService.getEnrolmentsByBranchAndYear(branchId, academicYear, PageRequest.of(page, size))));
    }

    @PutMapping("/{enrolmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update an enrolment")
    public ResponseEntity<ApiResponse<StudentEnrolmentResponse>> updateEnrolment(@PathVariable UUID enrolmentId,
                                                                                  @Valid @RequestBody UpdateEnrolmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(enrolmentService.updateEnrolment(enrolmentId, request), "Enrolment updated successfully"));
    }

    @DeleteMapping("/{enrolmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete an enrolment")
    public ResponseEntity<ApiResponse<Void>> deleteEnrolment(@PathVariable UUID enrolmentId) {
        enrolmentService.deleteEnrolment(enrolmentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Enrolment deleted successfully"));
    }
}
