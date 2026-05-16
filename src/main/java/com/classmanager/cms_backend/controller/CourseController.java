package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateCourseRequest;
import com.classmanager.cms_backend.dto.request.CreateSubjectRequest;
import com.classmanager.cms_backend.dto.request.UpdateCourseRequest;
import com.classmanager.cms_backend.dto.request.UpdateSubjectRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.CourseResponse;
import com.classmanager.cms_backend.dto.response.SubjectResponse;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import com.classmanager.cms_backend.service.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course catalogue, subject groups and subject management")
public class CourseController extends BaseController {

    private final CourseService courseService;

    @GetMapping("/subjects")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all active subjects (master catalogue)")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> listSubjects() {
        return ResponseEntity.ok(ApiResponse.success(courseService.listAllSubjects()));
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new subject in the master catalogue")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseService.createSubject(request), "Subject created successfully"));
    }

    @PutMapping("/subjects/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update an existing subject in the master catalogue")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable UUID subjectId,
            @Valid @RequestBody UpdateSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseService.updateSubject(subjectId, request), "Subject updated successfully"));
    }

    @DeleteMapping("/subjects/{subjectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Soft-delete a subject from the master catalogue")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable UUID subjectId) {
        courseService.deleteSubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subject deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all active courses")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> listCourses(
            @RequestParam(required = false) BoardType board,
            @RequestParam(required = false) CourseCategory category,
            @RequestParam(required = false) String standard) {
        return ResponseEntity.ok(ApiResponse.success(courseService.listCourses(board, category, standard)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new course with optional subject groups")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseService.createCourse(request), "Course created successfully"));
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get course detail with subject groups and allowed extra subjects")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseDetail(courseId)));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a course and replace subject groups when provided")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseService.updateCourse(courseId, request), "Course updated successfully"));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Soft-delete a course from the master catalogue")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }
}
