package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.CourseSubjectResponse;
import com.classmanager.cms_backend.entity.Subject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AcademicSubjectResponseMapper {

    public CourseSubjectResponse toResponse(Subject subject) {
        return CourseSubjectResponse.builder()
                .id(subject.getId())
                .subjectId(StringUtils.hasText(subject.getDisplayCode()) ? subject.getDisplayCode() : "SUB-" + subject.getId())
                .subjectName(subject.getDisplayName())
                .subjectCode(subject.getCode())
                .status(Boolean.TRUE.equals(subject.getIsActive()) ? "Active" : "Inactive")
                .build();
    }
}
