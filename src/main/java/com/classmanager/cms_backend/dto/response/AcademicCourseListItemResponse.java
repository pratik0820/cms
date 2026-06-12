package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AcademicCourseListItemResponse {
    private UUID id;
    private UUID courseUuid;
    private String courseId;
    private String standard;
    private String board;
    private String medium;
    private String academicYear;
    private String courseName;
    private String batchName;
    private String batchTiming;
    private String section;
}
