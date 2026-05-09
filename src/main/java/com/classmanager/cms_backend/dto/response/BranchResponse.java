package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {

    private UUID id;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String email;
    private Boolean isActive;
    private long totalStudents;
    private long totalTeachers;
    private long totalAdmins;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
