package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AcademicCourseDetailResponse {
    private UUID id;
    private UUID courseId;
    private UUID batchId;
    private String batchCode;
    private String standard;
    private String board;
    private String medium;
    private String academicYear;
    private String courseName;
    private String batchName;
    private String batchTiming;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<CourseSubjectResponse> subjects;
}
