package com.classmanager.cms_backend.dto.request;

import lombok.Data;

@Data
public class UpdateSubjectRequest {
    private String code;
    private String displayName;
    private String shortName;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
}
