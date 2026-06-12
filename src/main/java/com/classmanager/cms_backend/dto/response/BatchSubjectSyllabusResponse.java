package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BatchSubjectSyllabusResponse {
    private UUID batchId;
    private String batchName;
    private String courseName;
    private UUID subjectId;
    private String subjectName;
    private List<ChapterProgressResponse> chapters;
}
