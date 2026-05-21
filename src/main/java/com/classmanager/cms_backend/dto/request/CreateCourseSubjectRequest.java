package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCourseSubjectRequest {

    @NotBlank(message = "Subject name is required")
    private String subjectName;
}
