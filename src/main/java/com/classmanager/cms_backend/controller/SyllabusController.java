package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.ChapterRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.ChapterResponse;
import com.classmanager.cms_backend.service.SyllabusService;
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
@RequestMapping("/api/academic/syllabus/chapters")
@RequiredArgsConstructor
@Tag(name = "Syllabus Management", description = "APIs for managing curriculum syllabus")
public class SyllabusController {

    private final SyllabusService syllabusService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get all chapters for a given course and subject")
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getChapters(
            @RequestParam UUID courseId,
            @RequestParam UUID subjectId) {
        return ResponseEntity.ok(ApiResponse.success(syllabusService.getChapters(courseId, subjectId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create a new chapter with subtopics")
    public ResponseEntity<ApiResponse<ChapterResponse>> createChapter(
            @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(syllabusService.saveChapter(request), "Chapter created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update an existing chapter and its subtopics")
    public ResponseEntity<ApiResponse<ChapterResponse>> updateChapter(
            @PathVariable UUID id,
            @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(syllabusService.updateChapter(id, request), "Chapter updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete a chapter and its subtopics")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(@PathVariable UUID id) {
        syllabusService.deleteChapter(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Chapter deleted successfully"));
    }
}
