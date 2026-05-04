package com.classmanager.cms_backend.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    private UUID id;
    private String title;
    private String message;
    private String type;
    private UUID referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
