package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class TimetableEntryRequest {
    private LocalDate classDate;

    private String dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private UUID subjectId;
    private UUID batchId;
    private String room;
    private String periodType = "CLASS";
}
