package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.BatchTiming;
import com.classmanager.cms_backend.enums.BatchType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class UpdateBatchRequest {
    private String name;
    private BatchType batchType;
    private BatchTiming timing;
    private String timingLabel;
    private LocalTime startTime;
    private LocalTime endTime;
    private String daysOfWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxStudents;
    private UUID classTeacherId;
    private String room;
    private Boolean isActive;
}
