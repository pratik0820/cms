package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.request.UpdateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.CourseSubjectResponse;
import com.classmanager.cms_backend.service.CourseSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/academic/courses/{courseId}/subjects")
@RequiredArgsConstructor
@Tag(name = "Academic Course Subjects", description = "Feature-based course subject APIs")
public class CourseSubjectController extends BaseController {

    private final CourseSubjectService courseSubjectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Add a subject to a course")
    public ResponseEntity<ApiResponse<CourseSubjectResponse>> addSubject(
            @PathVariable UUID courseId,
            @RequestParam(required = false) UUID branchId,
            @Valid @RequestBody CreateCourseSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                courseSubjectService.addSubject(courseId, branchId, currentBranchId(), request),
                "Subject added successfully"));
    }

    @PutMapping("/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a course subject")
    public ResponseEntity<ApiResponse<CourseSubjectResponse>> updateSubject(
            @PathVariable UUID courseId,
            @PathVariable UUID subjectId,
            @RequestParam(required = false) UUID branchId,
            @Valid @RequestBody UpdateCourseSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                courseSubjectService.updateSubject(courseId, subjectId, branchId, currentBranchId(), request),
                "Subject updated successfully"));
    }

    @DeleteMapping("/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a course subject")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable UUID courseId,
            @PathVariable UUID subjectId,
            @RequestParam(required = false) UUID branchId) {
        courseSubjectService.deleteSubject(courseId, subjectId, branchId, currentBranchId());
        return ResponseEntity.ok(ApiResponse.success(null, "Subject deleted successfully"));
    }
}
