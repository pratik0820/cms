package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProgressRequest {

    @NotNull(message = "Batch ID is required")
    private UUID batchId;

    @NotNull(message = "Subtopic ID is required")
    private UUID subtopicId;

    private Integer completedQuestions;

    private String status;
}
