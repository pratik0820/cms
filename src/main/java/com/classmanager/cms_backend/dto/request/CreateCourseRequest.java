package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateCourseRequest {

    @NotBlank(message = "Course name is required")
    private String name;

    @NotBlank(message = "Course code is required")
    private String code;

    @NotNull(message = "Course category is required")
    private CourseCategory category;

    private BoardType board;

    @NotBlank(message = "Standard is required")
    private String standard;

    private String academicYear;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;

    @Valid
    private List<CourseSubjectGroupRequest> subjectGroups;
}
