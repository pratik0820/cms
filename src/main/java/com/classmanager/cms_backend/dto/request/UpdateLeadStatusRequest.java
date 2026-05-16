package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateLeadStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String notes;
    private String modeOfContact;
    private LocalDateTime nextFollowUpAt;
}
