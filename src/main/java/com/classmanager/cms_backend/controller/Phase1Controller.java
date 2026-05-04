package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.Phase1RecordRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.dto.response.Phase1RecordResponse;
import com.classmanager.cms_backend.service.Phase1Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Phase 1 Modules", description = "Phase 1 operational workflows from the requirements document")
public class Phase1Controller extends BaseController {

    private final Phase1Service phase1Service;

    @PostMapping("/api/leads")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> createLead(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.LEAD, request), "Inquiry captured successfully.");
    }

    @GetMapping("/api/leads")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listLeads(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.LEAD, branchId, page, size);
    }

    @PostMapping("/api/leads/{id}/convert")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> convertLead(
            @PathVariable UUID id,
            @RequestBody Phase1RecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(phase1Service.convertLead(id, request), "Lead converted."));
    }

    @PostMapping("/api/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> markAttendance(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.ATTENDANCE, request), "Attendance saved.");
    }

    @GetMapping("/api/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listAttendance(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.ATTENDANCE, branchId, page, size);
    }

    @PostMapping("/api/teacher/classes/start")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> startClass(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.startClass(request), "Class marked IN.");
    }

    @PostMapping("/api/teacher/classes/{sessionId}/end")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> endClass(
            @PathVariable UUID sessionId,
            @RequestBody Phase1RecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(phase1Service.endClass(sessionId, request), "Class marked OUT."));
    }

    @GetMapping("/api/teacher/{teacherId}/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> teacherAnalytics(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(ApiResponse.success(phase1Service.teacherAnalytics(teacherId)));
    }

    @PostMapping("/api/timetables")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> createTimetable(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.TIMETABLE, request), "Timetable entry saved.");
    }

    @GetMapping("/api/timetables")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listTimetables(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return paged(Phase1Service.TIMETABLE, branchId, page, size);
    }

    @PostMapping("/api/syllabus")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> uploadSyllabus(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.SYLLABUS, request), "Syllabus saved.");
    }

    @GetMapping("/api/syllabus")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listSyllabus(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return paged(Phase1Service.SYLLABUS, branchId, page, size);
    }

    @PostMapping("/api/syllabus/progress")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> updateSyllabusProgress(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.SYLLABUS_PROGRESS, request), "Syllabus progress updated.");
    }

    @GetMapping("/api/syllabus/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listSyllabusProgress(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return paged(Phase1Service.SYLLABUS_PROGRESS, branchId, page, size);
    }

    @PostMapping("/api/homeworks")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> createHomework(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.HOMEWORK, request), "Homework assigned.");
    }

    @GetMapping("/api/homeworks")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listHomework(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.HOMEWORK, branchId, page, size);
    }

    @PostMapping("/api/assignments")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> createAssignment(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.ASSIGNMENT, request), "Assignment assigned.");
    }

    @GetMapping("/api/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listAssignments(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.ASSIGNMENT, branchId, page, size);
    }

    @PostMapping("/api/tests")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> createTest(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.TEST, request), "MCQ test created.");
    }

    @GetMapping("/api/tests")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listTests(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.TEST, branchId, page, size);
    }

    @PostMapping("/api/student/tests/{testId}/attempts")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> submitTestAttempt(
            @PathVariable UUID testId,
            @RequestBody Phase1RecordRequest request) {
        request.setSubjectId(testId);
        return created(phase1Service.create(Phase1Service.TEST_ATTEMPT, request), "Test attempt recorded.");
    }

    @GetMapping("/api/students/{studentId}/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> studentPerformance(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(phase1Service.studentPerformance(studentId)));
    }

    @PostMapping("/api/feedback/lecture")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> submitLectureFeedback(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.FEEDBACK, request), "Lecture feedback submitted.");
    }

    @GetMapping("/api/feedback/lecture")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listLectureFeedback(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.FEEDBACK, branchId, page, size);
    }

    @PostMapping("/api/fees")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> createFeeRecord(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.FEE, request), "Fee record saved.");
    }

    @GetMapping("/api/fees")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listFees(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.FEE, branchId, page, size);
    }

    @PostMapping("/api/stationery/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> createStationeryRecord(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.STATIONERY, request), "Stationery record saved.");
    }

    @GetMapping("/api/stationery/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> listStationery(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.STATIONERY, branchId, page, size);
    }

    @PostMapping("/api/announcements")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> createAnnouncement(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.ANNOUNCEMENT, request), "Announcement published.");
    }

    @GetMapping("/api/student/announcements")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> studentAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paged(Phase1Service.ANNOUNCEMENT, null, page, size);
    }

    @PostMapping("/api/teacher/remarks")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> addTeacherRemark(@RequestBody Phase1RecordRequest request) {
        return created(phase1Service.create(Phase1Service.TEACHER_REMARK, request), "Teacher remark saved.");
    }

    @GetMapping("/api/dashboard/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboardSummary(@RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(phase1Service.dashboardSummary(branchId)));
    }

    @GetMapping("/api/phase1/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> getRecord(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(phase1Service.get(id)));
    }

    @PutMapping("/api/phase1/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> updateRecord(
            @PathVariable UUID id,
            @RequestBody Phase1RecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(phase1Service.update(id, request), "Record updated."));
    }

    @PatchMapping("/api/phase1/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Phase1RecordResponse>> patchRecord(
            @PathVariable UUID id,
            @RequestBody Phase1RecordRequest request) {
        return updateRecord(id, request);
    }

    @DeleteMapping("/api/phase1/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable UUID id) {
        phase1Service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Record deleted."));
    }

    private ResponseEntity<ApiResponse<Phase1RecordResponse>> created(Phase1RecordResponse response, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, message));
    }

    private ResponseEntity<ApiResponse<PagedResponse<Phase1RecordResponse>>> paged(
            String module, UUID branchId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Phase1RecordResponse> result = phase1Service.list(module, branchId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(result, result.getContent())));
    }
}
