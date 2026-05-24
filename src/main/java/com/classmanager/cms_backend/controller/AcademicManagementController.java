package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.request.CreateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.request.CreateStandardRequest;
import com.classmanager.cms_backend.dto.request.UpdateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.request.UpdateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.request.UpdateStandardRequest;
import com.classmanager.cms_backend.dto.response.AcademicCourseDetailResponse;
import com.classmanager.cms_backend.dto.response.AcademicCourseListItemResponse;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.BoardOptionResponse;
import com.classmanager.cms_backend.dto.response.CourseSubjectResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.dto.response.StandardResponse;
import com.classmanager.cms_backend.service.AcademicManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/academic")
@RequiredArgsConstructor
@Tag(name = "Academic Management", description = "Screen-focused standards, courses, batches, and subjects APIs")
public class AcademicManagementController extends BaseController {

    private final AcademicManagementService academicManagementService;

    @GetMapping("/boards")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List available boards for the standards and course screens")
    public ResponseEntity<ApiResponse<List<BoardOptionResponse>>> listBoards() {
        return ResponseEntity.ok(ApiResponse.success(academicManagementService.listBoards()));
    }

    @GetMapping("/subjects")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all global active subjects for forms")
    public ResponseEntity<ApiResponse<List<CourseSubjectResponse>>> listSubjects() {
        return ResponseEntity.ok(ApiResponse.success(academicManagementService.listGlobalSubjects()));
    }

    @GetMapping("/standards")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List standards and boards for the Standards & Boards screen")
    public ResponseEntity<ApiResponse<PagedResponse<StandardResponse>>> listStandards(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(academicManagementService.listStandards(search, page, size)));
    }

    @GetMapping("/standards/options")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active standards for the course form dropdown")
    public ResponseEntity<ApiResponse<List<StandardResponse>>> listStandardOptions() {
        return ResponseEntity.ok(ApiResponse.success(academicManagementService.listStandardOptions()));
    }

    @PostMapping("/standards")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a standard from the Add Standard modal")
    public ResponseEntity<ApiResponse<StandardResponse>> createStandard(@Valid @RequestBody CreateStandardRequest request) {
        return ResponseEntity.ok(ApiResponse.success(academicManagementService.createStandard(request), "Standard created successfully"));
    }

    @PutMapping("/standards/{standardId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a standard from the Standards & Boards screen")
    public ResponseEntity<ApiResponse<StandardResponse>> updateStandard(
            @PathVariable UUID standardId,
            @Valid @RequestBody UpdateStandardRequest request) {
        return ResponseEntity.ok(ApiResponse.success(academicManagementService.updateStandard(standardId, request), "Standard updated successfully"));
    }

    @DeleteMapping("/standards/{standardId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a standard from the Standards & Boards screen")
    public ResponseEntity<ApiResponse<Void>> deleteStandard(@PathVariable UUID standardId) {
        academicManagementService.deleteStandard(standardId);
        return ResponseEntity.ok(ApiResponse.success(null, "Standard deleted successfully"));
    }

    @GetMapping("/courses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List courses for the Courses screen")
    public ResponseEntity<ApiResponse<PagedResponse<AcademicCourseListItemResponse>>> listCourses(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                academicManagementService.listCourses(branchId, currentBranchId(), search, page, size)));
    }

    @PostMapping("/courses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a course and its batch from the Add New Course screen")
    public ResponseEntity<ApiResponse<AcademicCourseDetailResponse>> createCourse(
            @RequestParam(required = false) UUID branchId,
            @Valid @RequestBody CreateAcademicCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                academicManagementService.createCourse(branchId, currentBranchId(), request),
                "Course created successfully"));
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get course details and subjects for the View Course screen")
    public ResponseEntity<ApiResponse<AcademicCourseDetailResponse>> getCourse(
            @PathVariable UUID courseId,
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(
                academicManagementService.getCourse(courseId, branchId, currentBranchId())));
    }

    @PutMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a course and its batch details from the Edit Course screen")
    public ResponseEntity<ApiResponse<AcademicCourseDetailResponse>> updateCourse(
            @PathVariable UUID courseId,
            @RequestParam(required = false) UUID branchId,
            @Valid @RequestBody UpdateAcademicCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                academicManagementService.updateCourse(courseId, branchId, currentBranchId(), request),
                "Course updated successfully"));
    }

    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a course row from the Courses screen")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable UUID courseId,
            @RequestParam(required = false) UUID branchId) {
        academicManagementService.deleteCourse(courseId, branchId, currentBranchId());
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }

    @PostMapping("/courses/{courseId}/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Add a subject from the View Course screen")
    public ResponseEntity<ApiResponse<CourseSubjectResponse>> addSubject(
            @PathVariable UUID courseId,
            @RequestParam(required = false) UUID branchId,
            @Valid @RequestBody CreateCourseSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                academicManagementService.addSubject(courseId, branchId, currentBranchId(), request),
                "Subject added successfully"));
    }

    @PutMapping("/courses/{courseId}/subjects/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a subject name from the View Course screen")
    public ResponseEntity<ApiResponse<CourseSubjectResponse>> updateSubject(
            @PathVariable UUID courseId,
            @PathVariable UUID subjectId,
            @RequestParam(required = false) UUID branchId,
            @Valid @RequestBody UpdateCourseSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                academicManagementService.updateSubject(courseId, subjectId, branchId, currentBranchId(), request),
                "Subject updated successfully"));
    }

    @DeleteMapping("/courses/{courseId}/subjects/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a subject from the View Course screen")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable UUID courseId,
            @PathVariable UUID subjectId,
            @RequestParam(required = false) UUID branchId) {
        academicManagementService.deleteSubject(courseId, subjectId, branchId, currentBranchId());
        return ResponseEntity.ok(ApiResponse.success(null, "Subject deleted successfully"));
    }
}
