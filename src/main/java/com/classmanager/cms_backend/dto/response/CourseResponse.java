package com.classmanager.cms_backend.dto.response;

import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CourseResponse {
    private UUID id;
    private String name;
    private String code;
    private CourseCategory category;
    private BoardType board;
    private String standard;
    private String academicYear;
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
    /** Populated only in detail view. */
    private List<SubjectGroupResponse> subjectGroups;
}
