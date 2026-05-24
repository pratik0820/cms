package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LeadFollowUpResponse {
    private UUID id;
    private UUID leadId;
    private String leadCode;
    private String studentName;
    private LocalDateTime followUpAt;
    private String followUpType;
    private String followUpMode;
    private String spokeWith;
    private String notes;
    private String outcomeStatus;
    private String remarks;
    
    private LocalDateTime nextFollowUpAt;
    private String nextFollowUpType;
    private String nextFollowUpMode;
    private UUID nextFollowUpByUserId;
    private String nextFollowUpByName;
    private String reminder;
    private String priority;
    private String nextFollowUpNotes;

    private String followUpByName;
    private LocalDateTime createdAt;
}
