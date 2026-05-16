package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseSubjectGroupRequest {

    @NotBlank(message = "Subject group name is required")
    private String name;

    private String shortName;

    @NotEmpty(message = "At least one subject is required in a subject group")
    private List<UUID> subjectIds;

    private Boolean isExtraSubjectAllowed;
    private Integer maxExtraSubjects;
    private List<UUID> allowedExtraSubjectIds;
    private Integer sortOrder;
    private Boolean isActive;
}
