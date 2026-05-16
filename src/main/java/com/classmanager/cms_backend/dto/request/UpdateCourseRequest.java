package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCourseRequest {
    private String name;
    private String code;
    private CourseCategory category;
    private BoardType board;
    private String standard;
    private String academicYear;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;

    @Valid
    private List<CourseSubjectGroupRequest> subjectGroups;
}
