package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.request.UpdateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.response.AcademicCourseDetailResponse;
import com.classmanager.cms_backend.dto.response.AcademicCourseListItemResponse;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/academic/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course and batch-oriented academic APIs")
public class CourseController extends BaseController {

    private final CourseService courseService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List courses")
    public ResponseEntity<ApiResponse<PagedResponse<AcademicCourseListItemResponse>>> listCourses(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(courseService.listCourses(branchId, currentBranchId(), search, page, size)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a course and its batch")
    public ResponseEntity<ApiResponse<AcademicCourseDetailResponse>> createCourse(@RequestParam(required = false) UUID branchId,
                                                                                  @Valid @RequestBody CreateAcademicCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseService.createCourse(branchId, currentBranchId(), request), "Course created successfully"));
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get course details")
    public ResponseEntity<ApiResponse<AcademicCourseDetailResponse>> getCourse(@PathVariable UUID courseId,
                                                                               @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourse(courseId, branchId, currentBranchId())));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a course and its batch details")
    public ResponseEntity<ApiResponse<AcademicCourseDetailResponse>> updateCourse(@PathVariable UUID courseId,
                                                                                  @RequestParam(required = false) UUID branchId,
                                                                                  @Valid @RequestBody UpdateAcademicCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseService.updateCourse(courseId, branchId, currentBranchId(), request), "Course updated successfully"));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a course")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID courseId,
                                                          @RequestParam(required = false) UUID branchId) {
        courseService.deleteCourse(courseId, branchId, currentBranchId());
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }
}
