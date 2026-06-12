package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SubtopicRequest {
    private UUID id;

    @NotBlank(message = "Subtopic name is required")
    private String name;

    private Integer noOfQuestions;
}
