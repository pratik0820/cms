package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChapterProgressResponse {
    private UUID id;
    private String name;
    private java.time.LocalDate targetDate;
    private List<SubtopicProgressResponse> subtopics;
}
