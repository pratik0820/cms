package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String loginId;
    private LocalDate dateOfBirth;
    private String gender;
    private String profilePhotoUrl;
    private String qualification;
    private Integer experienceYears;
    private List<String> subjects;
    private List<UUID> subjectIds;
    private List<UUID> courseIds;
    private List<String> courseNames;
    private List<UUID> batchIds;
    private List<String> batchNames;
    private String specialization;
    private LocalDate joiningDate;
    private String employmentType;
    private String salaryType;
    private BigDecimal hourlyRate;
    private String address;
    private UUID branchId;
    private String branchName;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
