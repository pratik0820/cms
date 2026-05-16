package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.BatchTiming;
import com.classmanager.cms_backend.enums.BatchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateBatchRequest {

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    /** Optional — auto-generated from course + timing + year if blank. */
    private String name;

    @NotBlank(message = "Academic year is required (e.g. 2026-27)")
    private String academicYear;

    /** Batch type / tier. Optional — can be set later. */
    private BatchType batchType;

    @NotNull(message = "Timing is required")
    private BatchTiming timing;

    /** Required when timing = CUSTOM. */
    private String timingLabel;

    private LocalTime startTime;
    private LocalTime endTime;

    /** Comma-separated days, e.g. "MON,TUE,WED,THU,FRI,SAT". */
    private String daysOfWeek;

    private LocalDate startDate;
    private LocalDate endDate;

    /** 0 = unlimited. */
    private Integer maxStudents = 0;

    private UUID classTeacherId;
    private String room;
}
