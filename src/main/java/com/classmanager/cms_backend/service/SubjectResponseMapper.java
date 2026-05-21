package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.SubjectResponse;
import com.classmanager.cms_backend.entity.Subject;
import org.springframework.stereotype.Component;

@Component
public class SubjectResponseMapper {

    public SubjectResponse toSubjectResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .displayName(subject.getDisplayName())
                .shortName(subject.getShortName())
                .description(subject.getDescription())
                .sortOrder(subject.getSortOrder())
                .isActive(subject.getIsActive())
                .build();
    }
}
