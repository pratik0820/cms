package com.classmanager.cms_backend.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdateLeadFollowUpRequest {

    private String followUpType;
    private String followUpMode;
    private LocalDateTime followUpAt;
    private String spokeWith;
    private String notes;
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
