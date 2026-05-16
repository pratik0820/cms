package com.classmanager.cms_backend.dto.response;

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
public class LeadConversionResponse {

    private UUID leadId;
    private String leadCode;
    private UUID studentId;
    private String visibleStudentId;
    private LocalDateTime convertedAt;
    private StudentEnrolmentResponse enrolment;
}
