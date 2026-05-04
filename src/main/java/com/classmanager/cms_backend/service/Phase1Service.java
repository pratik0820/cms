package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.Phase1RecordRequest;
import com.classmanager.cms_backend.dto.response.Phase1RecordResponse;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.Phase1Record;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.Phase1RecordRepository;
import com.classmanager.cms_backend.repository.StudentRepository;
import com.classmanager.cms_backend.repository.TeacherRepository;
import com.classmanager.cms_backend.tenant.TenantContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class Phase1Service extends BaseService {

    public static final String LEAD = "LEAD";
    public static final String ATTENDANCE = "ATTENDANCE";
    public static final String CLASS_SESSION = "CLASS_SESSION";
    public static final String TIMETABLE = "TIMETABLE";
    public static final String SYLLABUS = "SYLLABUS";
    public static final String SYLLABUS_PROGRESS = "SYLLABUS_PROGRESS";
    public static final String HOMEWORK = "HOMEWORK";
    public static final String ASSIGNMENT = "ASSIGNMENT";
    public static final String TEST = "TEST";
    public static final String TEST_ATTEMPT = "TEST_ATTEMPT";
    public static final String FEEDBACK = "FEEDBACK";
    public static final String FEE = "FEE";
    public static final String STATIONERY = "STATIONERY";
    public static final String ANNOUNCEMENT = "ANNOUNCEMENT";
    public static final String TEACHER_REMARK = "TEACHER_REMARK";

    private final Phase1RecordRepository recordRepository;
    private final BranchRepository branchRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Phase1RecordResponse create(String module, Phase1RecordRequest request) {
        UUID tenantId = requireTenant();
        Phase1Record record = new Phase1Record();
        record.setTenantId(tenantId);
        record.setModule(module);
        applyRequest(record, request, tenantId);
        record.setCreatedByUserId(getCurrentUserId());
        if (!StringUtils.hasText(record.getStatus())) {
            record.setStatus(defaultStatus(module));
        }
        return map(recordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public Page<Phase1RecordResponse> list(String module, UUID branchId, Pageable pageable) {
        UUID tenantId = requireTenant();
        Page<Phase1Record> page = branchId == null
                ? recordRepository.findByTenantIdAndModuleAndIsDeletedFalseOrderByCreatedAtDesc(tenantId, module, pageable)
                : recordRepository.findByTenantIdAndModuleAndBranchIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        tenantId, module, branchId, pageable);
        return page.map(this::map);
    }

    @Transactional(readOnly = true)
    public Phase1RecordResponse get(UUID id) {
        return map(findRecord(id));
    }

    @Transactional
    public Phase1RecordResponse update(UUID id, Phase1RecordRequest request) {
        Phase1Record record = findRecord(id);
        applyRequest(record, request, record.getTenantId());
        return map(recordRepository.save(record));
    }

    @Transactional
    public void delete(UUID id) {
        Phase1Record record = findRecord(id);
        record.softDelete();
        recordRepository.save(record);
    }

    @Transactional
    public Phase1RecordResponse convertLead(UUID id, Phase1RecordRequest request) {
        Phase1Record lead = findRecord(id);
        lead.setStatus("CONVERTED");
        mergeDetails(lead, Map.of("conversion", request.getDetails() == null ? Map.of() : request.getDetails()));
        return map(recordRepository.save(lead));
    }

    @Transactional
    public Phase1RecordResponse startClass(Phase1RecordRequest request) {
        if (request.getStartTime() == null) {
            request.setStartTime(LocalDateTime.now());
        }
        if (request.getEventDate() == null) {
            request.setEventDate(request.getStartTime().toLocalDate());
        }
        request.setStatus("IN_PROGRESS");
        request.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "Class session");
        return create(CLASS_SESSION, request);
    }

    @Transactional
    public Phase1RecordResponse endClass(UUID sessionId, Phase1RecordRequest request) {
        Phase1Record session = findRecord(sessionId);
        if (!CLASS_SESSION.equals(session.getModule())) {
            throw new ResourceNotFoundException("Class session", sessionId);
        }
        LocalDateTime end = request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now();
        session.setEndTime(end);
        session.setStatus("COMPLETED");
        if (session.getStartTime() != null) {
            session.setDurationMinutes((int) Duration.between(session.getStartTime(), end).toMinutes());
        }
        if (StringUtils.hasText(request.getDescription())) {
            session.setDescription(request.getDescription());
        }
        mergeDetails(session, request.getDetails());
        return map(recordRepository.save(session));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboardSummary(UUID branchId) {
        UUID tenantId = requireTenant();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalStudents", branchId == null
                ? studentRepository.countByTenantIdAndIsDeletedFalse(tenantId)
                : studentRepository.countByBranchIdAndIsDeletedFalse(branchId));
        summary.put("totalTeachers", teacherRepository.countByTenantIdAndIsDeletedFalse(tenantId));
        summary.put("totalFeesCollected", recordRepository.sumPaid(tenantId, FEE));
        summary.put("pendingFees", recordRepository.sumPending(tenantId, FEE));
        summary.put("activeClasses", recordRepository.countByTenantIdAndModuleAndStatusAndIsDeletedFalse(
                tenantId, CLASS_SESSION, "IN_PROGRESS"));
        summary.put("attendanceRecords", recordRepository.countByTenantIdAndModuleAndIsDeletedFalse(tenantId, ATTENDANCE));
        summary.put("openLeads", recordRepository.countByTenantIdAndModuleAndStatusAndIsDeletedFalse(tenantId, LEAD, "OPEN"));
        summary.put("testsCreated", recordRepository.countByTenantIdAndModuleAndIsDeletedFalse(tenantId, TEST));
        summary.put("homeworkAssigned", recordRepository.countByTenantIdAndModuleAndIsDeletedFalse(tenantId, HOMEWORK));
        summary.put("stationeryIssued", recordRepository.countByTenantIdAndModuleAndIsDeletedFalse(tenantId, STATIONERY));
        return summary;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> teacherAnalytics(UUID teacherId) {
        UUID tenantId = requireTenant();
        List<Phase1Record> sessions = recordRepository
                .findByTenantIdAndModuleAndTeacherIdAndIsDeletedFalseOrderByEventDateDesc(tenantId, CLASS_SESSION, teacherId);
        int minutes = sessions.stream().map(Phase1Record::getDurationMinutes).filter(v -> v != null).mapToInt(Integer::intValue).sum();
        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("lecturesTaken", sessions.size());
        analytics.put("hoursTaken", BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP));
        analytics.put("syllabusUpdates", recordRepository
                .findByTenantIdAndModuleAndTeacherIdAndIsDeletedFalseOrderByEventDateDesc(tenantId, SYLLABUS_PROGRESS, teacherId)
                .size());
        return analytics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> studentPerformance(UUID studentId) {
        UUID tenantId = requireTenant();
        List<Phase1Record> attendance = recordRepository
                .findByTenantIdAndModuleAndStudentIdAndIsDeletedFalseOrderByEventDateDesc(tenantId, ATTENDANCE, studentId);
        List<Phase1Record> attempts = recordRepository
                .findByTenantIdAndModuleAndStudentIdAndIsDeletedFalseOrderByEventDateDesc(tenantId, TEST_ATTEMPT, studentId);
        long present = attendance.stream().filter(r -> "PRESENT".equalsIgnoreCase(r.getStatus())).count();
        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("attendanceRecords", attendance.size());
        analytics.put("presentRecords", present);
        analytics.put("attendancePercentage", attendance.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(present * 100.0 / attendance.size()).setScale(2, java.math.RoundingMode.HALF_UP));
        analytics.put("testsAttempted", attempts.size());
        analytics.put("marksHistory", attempts.stream().map(this::map).toList());
        return analytics;
    }

    private void applyRequest(Phase1Record record, Phase1RecordRequest request, UUID tenantId) {
        if (request == null) {
            return;
        }
        record.setType(request.getType());
        record.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : titleFor(record.getModule()));
        record.setDescription(request.getDescription());
        record.setStatus(request.getStatus());
        record.setBatchId(request.getBatchId());
        record.setSubjectId(request.getSubjectId());
        record.setStudentId(request.getStudentId());
        record.setTeacherId(request.getTeacherId());
        record.setSource(request.getSource());
        record.setEventDate(request.getEventDate() != null ? request.getEventDate() : LocalDate.now());
        record.setStartTime(request.getStartTime());
        record.setEndTime(request.getEndTime());
        record.setDurationMinutes(request.getDurationMinutes());
        record.setAmount(request.getAmount());
        record.setPaidAmount(request.getPaidAmount());
        record.setPendingAmount(request.getPendingAmount());
        record.setScore(request.getScore());
        record.setMaxScore(request.getMaxScore());
        record.setPercentage(request.getPercentage());
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findByIdAndTenantIdAndIsDeletedFalse(request.getBranchId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            record.setBranch(branch);
        }
        writeDetails(record, request.getDetails());
    }

    private Phase1Record findRecord(UUID id) {
        Phase1Record record = recordRepository.findById(id)
                .filter(r -> !r.isDeleted() && r.getTenantId().equals(requireTenant()))
                .orElseThrow(() -> new ResourceNotFoundException("Phase1 record", id));
        return record;
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            tenantId = getCurrentTenantId();
        }
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is required");
        }
        return tenantId;
    }

    private String defaultStatus(String module) {
        return switch (module) {
            case LEAD -> "OPEN";
            case HOMEWORK, ASSIGNMENT, TEST -> "PUBLISHED";
            case FEE -> "PENDING";
            default -> "ACTIVE";
        };
    }

    private String titleFor(String module) {
        return module.toLowerCase().replace('_', ' ');
    }

    private void writeDetails(Phase1Record record, Map<String, Object> details) {
        try {
            record.setDetailsJson(details == null ? "{}" : objectMapper.writeValueAsString(details));
        } catch (Exception ex) {
            throw new IllegalArgumentException("details must be JSON serializable", ex);
        }
    }

    private void mergeDetails(Phase1Record record, Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        Map<String, Object> merged = readDetails(record.getDetailsJson());
        merged.putAll(details);
        writeDetails(record, merged);
    }

    private Map<String, Object> readDetails(String detailsJson) {
        if (!StringUtils.hasText(detailsJson)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(detailsJson, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private Phase1RecordResponse map(Phase1Record r) {
        return Phase1RecordResponse.builder()
                .id(r.getId())
                .module(r.getModule())
                .type(r.getType())
                .title(r.getTitle())
                .description(r.getDescription())
                .status(r.getStatus())
                .branchId(r.getBranch() != null ? r.getBranch().getId() : null)
                .branchName(r.getBranch() != null ? r.getBranch().getName() : null)
                .batchId(r.getBatchId())
                .subjectId(r.getSubjectId())
                .studentId(r.getStudentId())
                .teacherId(r.getTeacherId())
                .createdByUserId(r.getCreatedByUserId())
                .source(r.getSource())
                .eventDate(r.getEventDate())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .durationMinutes(r.getDurationMinutes())
                .amount(r.getAmount())
                .paidAmount(r.getPaidAmount())
                .pendingAmount(r.getPendingAmount())
                .score(r.getScore())
                .maxScore(r.getMaxScore())
                .percentage(r.getPercentage())
                .details(readDetails(r.getDetailsJson()))
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
