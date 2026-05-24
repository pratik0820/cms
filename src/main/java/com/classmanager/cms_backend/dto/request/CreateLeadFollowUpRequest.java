package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateLeadFollowUpRequest {

    @NotBlank(message = "Follow-up type is required")
    private String followUpType;

    @NotBlank(message = "Follow-up mode is required")
    private String followUpMode;

    @NotNull(message = "Follow-up date and time is required")
    private LocalDateTime followUpAt;

    @NotBlank(message = "Spoke with is required")
    private String spokeWith;

    @NotBlank(message = "Conversation notes are required")
    private String notes;

    @NotBlank(message = "Outcome status is required")
    private String outcomeStatus; // COMPLETED or PENDING

    private String remarks; // Internal remarks

    private LocalDateTime nextFollowUpAt;
    private String nextFollowUpType;
    private String nextFollowUpMode;
    private UUID nextFollowUpByUserId;
    private String reminder;
    private String priority;
    private String nextFollowUpNotes;
}
