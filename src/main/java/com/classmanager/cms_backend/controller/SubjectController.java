package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.request.UpdateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.CourseSubjectResponse;
import com.classmanager.cms_backend.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Global subject and course-subject APIs")
public class SubjectController extends BaseController {

    private final SubjectService subjectService;

    @GetMapping("/subjects")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all global active subjects for forms")
    public ResponseEntity<ApiResponse<List<CourseSubjectResponse>>> listSubjects() {
        return ResponseEntity.ok(ApiResponse.success(subjectService.listSubjects()));
    }

    @PostMapping("/courses/{courseId}/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Add a subject to a course")
    public ResponseEntity<ApiResponse<CourseSubjectResponse>> addSubject(@PathVariable UUID courseId,
                                                                         @RequestParam(required = false) UUID branchId,
                                                                         @Valid @RequestBody CreateCourseSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(subjectService.addCourseSubject(courseId, branchId, currentBranchId(), request), "Subject added successfully"));
    }

    @PutMapping("/courses/{courseId}/subjects/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a course subject")
    public ResponseEntity<ApiResponse<CourseSubjectResponse>> updateSubject(@PathVariable UUID courseId,
                                                                            @PathVariable UUID subjectId,
                                                                            @RequestParam(required = false) UUID branchId,
                                                                            @Valid @RequestBody UpdateCourseSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(subjectService.updateCourseSubject(courseId, subjectId, branchId, currentBranchId(), request), "Subject updated successfully"));
    }

    @DeleteMapping("/courses/{courseId}/subjects/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a course subject")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable UUID courseId,
                                                           @PathVariable UUID subjectId,
                                                           @RequestParam(required = false) UUID branchId) {
        subjectService.deleteCourseSubject(courseId, subjectId, branchId, currentBranchId());
        return ResponseEntity.ok(ApiResponse.success(null, "Subject deleted successfully"));
    }
}
