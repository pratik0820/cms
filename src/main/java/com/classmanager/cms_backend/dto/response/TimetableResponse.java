package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TimetableResponse {
    private UUID id;
    private UUID teacherId;
    private String teacherName;
    private LocalDate effectiveDate;
    private String viewType;
    private List<TimetableEntryResponse> entries;
}
