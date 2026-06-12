package com.classmanager.cms_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class SaveTimetableRequest {
    @NotNull(message = "Teacher ID is required")
    private UUID teacherId;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private String viewType;

    @Valid
    private List<TimetableEntryRequest> entries;
}
