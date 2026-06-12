package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChapterResponse {
    private UUID id;
    private UUID courseId;
    private String courseName;
    private UUID subjectId;
    private String subjectName;
    private String name;
    private LocalDate targetDate;
    private List<SubtopicResponse> subtopics;
    private LocalDateTime createdAt;
}
