package com.classmanager.cms_backend.dto.response;

import com.classmanager.cms_backend.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String studentId;
    private UUID branchId;
    private String branchName;
    private String standard;
    private String batch;
    private String gender;
    private LocalDate dateOfBirth;
    private String mobile;
    private String parentName;
    private String parentPhone;
    private String email;
    private String address;
    private String schoolName;
    private BoardType board;
    private LocalDate admissionDate;
    private String loginId;
    private String profilePhotoUrl;
    private Boolean isAdmissionFinal;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<StudentEnrolmentResponse> enrolments;
}
