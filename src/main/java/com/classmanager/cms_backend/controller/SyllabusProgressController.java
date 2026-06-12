package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.UpdateProgressRequest;
import com.classmanager.cms_backend.dto.response.AcademicCourseListItemResponse;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.BatchSubjectSyllabusResponse;
import com.classmanager.cms_backend.dto.response.ChapterProgressResponse;
import com.classmanager.cms_backend.service.SyllabusProgressService;
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
@RequestMapping("/api/academic/syllabus/progress")
@RequiredArgsConstructor
@Tag(name = "Syllabus Progress", description = "APIs for tracking syllabus completion")
public class SyllabusProgressController extends BaseController {

    private final SyllabusProgressService progressService;

    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get assigned batches for the logged-in teacher")
    public ResponseEntity<ApiResponse<List<AcademicCourseListItemResponse>>> getMyAssignments() {
        return ResponseEntity.ok(ApiResponse.success(progressService.getMyAssignments(currentUserId())));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get all syllabus progress for the logged-in teacher")
    public ResponseEntity<ApiResponse<List<BatchSubjectSyllabusResponse>>> getAllMyProgress() {
        return ResponseEntity.ok(ApiResponse.success(progressService.getAllMyProgress(currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'TEACHER')")
    @Operation(summary = "Get syllabus progress for a specific batch and subject")
    public ResponseEntity<ApiResponse<List<ChapterProgressResponse>>> getBatchProgress(
            @RequestParam UUID batchId,
            @RequestParam UUID subjectId) {
        return ResponseEntity.ok(ApiResponse.success(progressService.getBatchProgress(batchId, subjectId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'TEACHER')")
    @Operation(summary = "Update progress for a specific subtopic in a batch")
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @Valid @RequestBody UpdateProgressRequest request) {
        progressService.updateProgress(request, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Progress updated successfully"));
    }
}
