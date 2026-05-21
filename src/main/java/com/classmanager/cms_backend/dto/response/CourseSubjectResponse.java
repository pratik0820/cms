package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CourseSubjectResponse {
    private UUID id;
    private String subjectId;
    private String subjectName;
    private String subjectCode;
    private String status;
}
