package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SubtopicProgressResponse {
    private UUID id;
    private String name;
    private Integer totalQuestions;
    private Integer completedQuestions;
    private String status; // PENDING, IN_PROGRESS, COMPLETED
}
