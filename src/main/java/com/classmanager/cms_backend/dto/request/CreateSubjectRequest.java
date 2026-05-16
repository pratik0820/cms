package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSubjectRequest {

    @NotBlank(message = "Subject code is required")
    private String code;

    @NotBlank(message = "Display name is required")
    private String displayName;

    private String shortName;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
}
