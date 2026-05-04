package com.classmanager.cms_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Phase1RecordResponse {
    private UUID id;
    private String module;
    private String type;
    private String title;
    private String description;
    private String status;
    private UUID branchId;
    private String branchName;
    private UUID batchId;
    private UUID subjectId;
    private UUID studentId;
    private UUID teacherId;
    private UUID createdByUserId;
    private String source;
    private LocalDate eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal percentage;
    private Map<String, Object> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
