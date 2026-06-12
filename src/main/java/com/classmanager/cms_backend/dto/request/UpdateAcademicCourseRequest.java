package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.BatchTiming;
import com.classmanager.cms_backend.enums.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class UpdateAcademicCourseRequest {

    @NotBlank(message = "Standard is required")
    private String standard;

    @NotNull(message = "Board is required")
    private BoardType board;

    @NotBlank(message = "Medium is required")
    private String medium;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotBlank(message = "Course name is required")
    private String courseName;

    @NotBlank(message = "Batch name is required")
    private String batchName;

    @NotNull(message = "Batch timing is required")
    private BatchTiming batchTiming;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String section;
}
