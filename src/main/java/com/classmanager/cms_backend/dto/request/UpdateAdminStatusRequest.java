package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAdminStatusRequest {

    @NotNull(message = "Status is required")
    private Boolean isActive;

    private String reason;
}
