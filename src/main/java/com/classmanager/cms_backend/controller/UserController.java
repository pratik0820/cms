package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateAdminRequest;
import com.classmanager.cms_backend.dto.request.CreateStudentRequest;
import com.classmanager.cms_backend.dto.request.CreateTeacherRequest;
import com.classmanager.cms_backend.dto.request.ResetPasswordRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.dto.response.UserResponse;
import com.classmanager.cms_backend.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Create and manage Admins, Teachers, and Students")
public class UserController extends BaseController {

    private final UserManagementService userManagementService;

    // ══════════════════════════════════════════════════════════════════════════
    // ADMIN ENDPOINTS
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/admins")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(
            summary = "Create an Admin account",
            description = """
            **Who can call this:** TENANT_OWNER only.
            
            Creates a new Admin user for the current institute.
            A random temporary password is auto-generated and returned **once** in this response.
            The institute owner must share these credentials with the new Admin.
            
            The Admin can change their password after first login via PUT /api/auth/change-password.
            """
    )
    public ResponseEntity<ApiResponse<UserResponse.AdminResponse>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request) {

        UserResponse.AdminResponse response = userManagementService.createAdmin(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Admin account created successfully."));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TEACHER ENDPOINTS
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(
            summary = "Register a new Teacher",
            description = """
            **Who can call this:** ADMIN or TENANT_OWNER.
            
            Registers a teacher and creates their login account in one step.
            
            **What the system generates automatically:**
            - Login credentials: teacher logs in with their email + the generated password
            - The generated password is returned **only once** in this response
            
            **After creation:**
            - Share the `generatedLoginEmail` and `generatedPassword` with the teacher
            - Teacher should change their password on first login via PUT /api/auth/change-password
            - Assign subjects via POST /api/teachers/{id}/subjects (separate endpoint)
            """
    )
    public ResponseEntity<ApiResponse<UserResponse.TeacherResponse>> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request) {

        UserResponse.TeacherResponse response = userManagementService.createTeacher(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,
                        "Teacher registered. Share the generated credentials with the teacher."));
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(summary = "List all Teachers (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse.TeacherResponse>>> listTeachers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<UserResponse.TeacherResponse> result = userManagementService.listTeachers(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(result, result.getContent())));
    }

    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(summary = "Get Teacher details by ID")
    public ResponseEntity<ApiResponse<UserResponse.TeacherResponse>> getTeacher(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(userManagementService.getTeacher(id)));
    }

    @PostMapping("/teachers/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(
            summary = "Reset a Teacher's password (admin override)",
            description = "Does NOT require the teacher's current password. " +
                    "Use when a teacher is locked out or has forgotten their password."
    )
    public ResponseEntity<ApiResponse<Void>> resetTeacherPassword(
            @PathVariable UUID id,
            @Valid @RequestBody ResetPasswordRequest request) {

        userManagementService.resetUserPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher password reset successfully."));
    }

    @PostMapping("/teachers/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(summary = "Deactivate a Teacher account (blocks login)")
    public ResponseEntity<ApiResponse<Void>> deactivateTeacher(@PathVariable UUID id) {
        userManagementService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher account deactivated."));
    }

    @PostMapping("/teachers/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(summary = "Reactivate a Teacher account")
    public ResponseEntity<ApiResponse<Void>> activateTeacher(@PathVariable UUID id) {
        userManagementService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher account activated."));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STUDENT ENDPOINTS
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(
            summary = "Enroll a new Student",
            description = """
            **Who can call this:** ADMIN or TENANT_OWNER.
            
            Enrolls a student and creates their login account.
            Admission is created in **DRAFT** state — it cannot be finalised until a photo is uploaded.
            
            **What the system generates automatically:**
            - `loginId` — unique ID e.g. `STU-A3X9KL` (student and parent both use this to log in)
            - `password` — temporary password returned **only once** in this response
            
            **Admission workflow:**
            1. POST /api/users/students              → Creates student in DRAFT state
            2. POST /api/users/students/{id}/photo   → Upload student photo (mandatory)
            3. POST /api/users/students/{id}/finalise → Locks in the admission
            
            **Parent access:**
            Parent logs in with the same `loginId` + `password` on the parent portal.
            """
    )
    public ResponseEntity<ApiResponse<UserResponse.StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {

        UserResponse.StudentResponse response = userManagementService.createStudent(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,
                        "Student enrolled in DRAFT state. Upload photo and then finalise admission."));
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(summary = "List all Students (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse.StudentResponse>>> listStudents(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<UserResponse.StudentResponse> result = userManagementService.listStudents(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(result, result.getContent())));
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(summary = "Get Student details by ID")
    public ResponseEntity<ApiResponse<UserResponse.StudentResponse>> getStudent(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(userManagementService.getStudent(id)));
    }

    @PostMapping(value = "/students/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(
            summary = "Upload Student photo",
            description = """
            Uploads the student's photo and saves the URL.
            **This is required before calling the /finalise endpoint.**

            Accepted formats: JPEG, PNG, WebP. Max size: 10MB.
            """
    )
    public ResponseEntity<ApiResponse<String>> uploadStudentPhoto(
            @PathVariable UUID id,
            @RequestParam("photo") MultipartFile photo) {

        String photoUrl = userManagementService.uploadStudentPhoto(id, photo);
        return ResponseEntity.ok(ApiResponse.success(photoUrl, "Photo uploaded successfully."));
    }

    @PostMapping("/students/{id}/finalise")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(
            summary = "Finalise student admission",
            description = """
            Locks in the student's admission.
            **Requires photo to have been uploaded first** — returns 422 if photo is missing.
            Once finalised, the student appears in all reports and attendance records.
            """
    )
    public ResponseEntity<ApiResponse<UserResponse.StudentResponse>> finaliseAdmission(
            @PathVariable UUID id) {

        UserResponse.StudentResponse response = userManagementService.finaliseAdmission(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Admission finalised successfully."));
    }

    @PostMapping("/students/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(
            summary = "Reset a Student's password (admin override)",
            description = "New password applies to both the student and parent login (shared credentials)."
    )
    public ResponseEntity<ApiResponse<Void>> resetStudentPassword(
            @PathVariable UUID id,
            @Valid @RequestBody ResetPasswordRequest request) {

        userManagementService.resetUserPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(null,
                "Password reset. Both student and parent can now log in with the new password."));
    }

    @PostMapping("/students/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(summary = "Deactivate a Student account (blocks login for student and parent)")
    public ResponseEntity<ApiResponse<Void>> deactivateStudent(@PathVariable UUID id) {
        userManagementService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Student account deactivated."));
    }

    @PostMapping("/students/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    @Operation(summary = "Reactivate a Student account")
    public ResponseEntity<ApiResponse<Void>> activateStudent(@PathVariable UUID id) {
        userManagementService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Student account activated."));
    }
}
