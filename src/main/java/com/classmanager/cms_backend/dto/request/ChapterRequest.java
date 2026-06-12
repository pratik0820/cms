package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ChapterRequest {
    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotBlank(message = "Chapter name is required")
    private String name;

    private LocalDate targetDate;

    private List<SubtopicRequest> subtopics;
}
