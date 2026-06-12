package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class TimetableEntryResponse {
    private UUID id;
    private LocalDate classDate;
    private String dayOfWeek;
    private UUID teacherId;
    private String teacherName;
    private LocalTime startTime;
    private LocalTime endTime;
    private UUID subjectId;
    private String subjectName;
    private UUID batchId;
    private String batchName;
    private String section;
    private String room;
    private String periodType;
}
