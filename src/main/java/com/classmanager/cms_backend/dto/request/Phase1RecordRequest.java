package com.classmanager.cms_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class Phase1RecordRequest {

    private String type;
    private String title;
    private String description;
    private String status;
    private UUID branchId;
    private UUID batchId;
    private UUID subjectId;
    private UUID studentId;
    private UUID teacherId;
    private String source;
    private LocalDate eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private Integer durationMinutes;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal percentage;

    /**
     * Module-specific fields, for example MCQ questions, selected topics,
     * homework attachments, feedback answers, or stationery item lines.
     */
    private Map<String, Object> details = new LinkedHashMap<>();
}
