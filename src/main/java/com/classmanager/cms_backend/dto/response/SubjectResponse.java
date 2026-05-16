package com.classmanager.cms_backend.dto.response;

import com.classmanager.cms_backend.enums.SubjectCode;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SubjectResponse {
    private UUID id;
    private SubjectCode code;
    private String displayName;
    private String shortName;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
}
