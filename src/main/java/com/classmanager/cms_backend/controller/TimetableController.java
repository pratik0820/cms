package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.SaveTimetableRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.TimetableResponse;
import com.classmanager.cms_backend.service.TimetableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic/timetables")
@RequiredArgsConstructor
@Tag(name = "Timetable Management", description = "APIs for managing class timetables")
public class TimetableController extends BaseController {

    private final TimetableService timetableService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get a timetable for a teacher and optional effective date")
    public ResponseEntity<ApiResponse<TimetableResponse>> getTimetable(
            @RequestParam UUID teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate) {
        
        TimetableResponse response = timetableService.getTimetable(teacherId, effectiveDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get all timetables across the school")
    public ResponseEntity<ApiResponse<List<TimetableResponse>>> getAllTimetables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate) {
        
        List<TimetableResponse> response = timetableService.getAllTimetables(effectiveDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Save or update a timetable for a teacher")
    public ResponseEntity<ApiResponse<TimetableResponse>> saveTimetable(
            @Valid @RequestBody SaveTimetableRequest request) {
        
        TimetableResponse response = timetableService.saveTimetable(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Timetable saved successfully"));
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get calendar timetables")
    public ResponseEntity<ApiResponse<List<com.classmanager.cms_backend.dto.response.TimetableEntryResponse>>> getCalendarTimetables(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID teacherId) {
        
        List<com.classmanager.cms_backend.dto.response.TimetableEntryResponse> response = timetableService.getCalendarTimetables(startDate, endDate, teacherId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/calendar")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get calendar timetables for the currently authenticated user")
    public ResponseEntity<ApiResponse<List<com.classmanager.cms_backend.dto.response.TimetableEntryResponse>>> getMyCalendarTimetables(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<com.classmanager.cms_backend.dto.response.TimetableEntryResponse> response = timetableService.getMyCalendarTimetables(startDate, endDate, currentUser());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Schedule a single class session")
    public ResponseEntity<ApiResponse<com.classmanager.cms_backend.dto.response.TimetableEntryResponse>> scheduleClass(
            @RequestParam UUID teacherId,
            @Valid @RequestBody com.classmanager.cms_backend.dto.request.TimetableEntryRequest request) {
        
        com.classmanager.cms_backend.dto.response.TimetableEntryResponse response = timetableService.scheduleClass(teacherId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Class scheduled successfully"));
    }

    @PutMapping("/entries/{entryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update a scheduled class session")
    public ResponseEntity<ApiResponse<com.classmanager.cms_backend.dto.response.TimetableEntryResponse>> updateScheduledClass(
            @PathVariable UUID entryId,
            @Valid @RequestBody com.classmanager.cms_backend.dto.request.TimetableEntryRequest request) {
        
        com.classmanager.cms_backend.dto.response.TimetableEntryResponse response = timetableService.updateScheduledClass(entryId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Class updated successfully"));
    }

    @DeleteMapping("/entries/{entryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete a scheduled class session")
    public ResponseEntity<ApiResponse<Void>> deleteScheduledClass(@PathVariable UUID entryId) {
        
        timetableService.deleteScheduledClass(entryId);
        return ResponseEntity.ok(ApiResponse.success(null, "Class deleted successfully"));
    }

    @DeleteMapping("/{timetableId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete a timetable")
    public ResponseEntity<ApiResponse<Void>> deleteTimetable(@PathVariable UUID timetableId) {
        
        timetableService.deleteTimetable(timetableId);
        return ResponseEntity.ok(ApiResponse.success(null, "Timetable deleted successfully"));
    }
}
