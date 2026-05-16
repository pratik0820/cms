package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SubjectGroupResponse {
    private UUID id;
    private String name;
    private String shortName;
    private Integer subjectCount;
    private Boolean isExtraSubjectAllowed;
    private Integer maxExtraSubjects;
    private List<SubjectResponse> subjects;
    private List<SubjectResponse> allowedExtraSubjects;
    private Integer sortOrder;
    private Boolean isActive;
}
