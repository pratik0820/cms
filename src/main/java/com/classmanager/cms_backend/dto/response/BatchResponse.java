package com.classmanager.cms_backend.dto.response;

import com.classmanager.cms_backend.enums.BatchTiming;
import com.classmanager.cms_backend.enums.BatchType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class BatchResponse {
    private UUID id;
    private UUID branchId;
    private String branchName;
    private UUID courseId;
    private String courseName;
    private String courseCode;
    private String name;
    private String academicYear;
    private BatchType batchType;
    private BatchTiming timing;
    private String timingLabel;
    private LocalTime startTime;
    private LocalTime endTime;
    private String daysOfWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxStudents;
    private Integer enrolledCount;
    private UUID classTeacherId;
    private String classTeacherName;
    private String room;
    private Boolean isActive;
}
