package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.CourseResponse;
import com.classmanager.cms_backend.dto.response.SubjectResponse;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import com.classmanager.cms_backend.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    // ── Subjects ─────────────────────────────────────────────────────────────

    @GetMapping("/subjects")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all active subjects (master catalogue)")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> listSubjects() {
        return ResponseEntity.ok(ApiResponse.success(courseService.listAllSubjects()));
    }

    // ── Courses ───────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all active courses")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> listCourses(
            @RequestParam(required = false) BoardType board,
            @RequestParam(required = false) CourseCategory category) {

        List<CourseResponse> courses;
        if (board != null) {
            courses = courseService.listCoursesByBoard(board);
        } else if (category != null) {
            courses = courseService.listCoursesByCategory(category);
        } else {
            courses = courseService.listAllCourses();
        }
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get course detail with subject groups and allowed extra subjects")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseDetail(courseId)));
    }
}
