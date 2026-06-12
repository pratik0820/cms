package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SubtopicResponse {
    private UUID id;
    private String name;
    private Integer noOfQuestions;
}
