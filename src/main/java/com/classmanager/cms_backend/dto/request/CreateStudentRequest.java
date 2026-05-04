package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.BoardType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for POST /api/users/students
 * Only ADMIN / SUPER_ADMIN can enroll students.
 *
 * After creation, the student record is in DRAFT state (is_admission_final = false).
 * A separate call POST /api/users/students/{id}/finalise is required to finalise,
 * which validates that photo_url is not null.
 */
@Data
public class CreateStudentRequest {

    // ── Personal Details ──────────────────────────────────────────────────────

    @NotBlank(message = "Student name is required")
    @Size(min = 2, max = 150, message = "Name must be 2–150 characters")
    private String name;

    private LocalDate dateOfBirth;

    private String gender;

    @NotBlank(message = "Mobile number is required")
    private String mobile;

    // ── Parent Details ────────────────────────────────────────────────────────

    @NotBlank(message = "Parent name is required")
    private String parentName;

    @NotBlank(message = "Parent phone is required")
    private String parentPhone;

    private String email;

    private String address;

    // ── Academic Details ──────────────────────────────────────────────────────

    private String schoolName;

    @NotBlank(message = "Standard (grade) is required")
    private String standard;

    @NotNull(message = "Board type is required (SSC, CBSE, or ICSE)")
    private BoardType board;

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    private UUID batchId;
}
