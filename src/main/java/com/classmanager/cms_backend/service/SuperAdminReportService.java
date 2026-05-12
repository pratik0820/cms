package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.GenerateReportRequest;
import com.classmanager.cms_backend.dto.response.ReportFile;
import com.classmanager.cms_backend.dto.response.ReportManagementResponse;
import com.classmanager.cms_backend.dto.response.ReportResponse;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.GeneratedReport;
import com.classmanager.cms_backend.entity.OperationalRecord;
import com.classmanager.cms_backend.entity.Student;
import com.classmanager.cms_backend.entity.Teacher;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.GeneratedReportRepository;
import com.classmanager.cms_backend.repository.OperationalRecordRepository;
import com.classmanager.cms_backend.repository.StudentRepository;
import com.classmanager.cms_backend.repository.TeacherRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class SuperAdminReportService {

    private static final String FORMAT_CSV = "CSV";
    private static final String FORMAT_PDF = "PDF";
    private static final String STORAGE_PROVIDER_ON_DEMAND = "ON_DEMAND";

    private final GeneratedReportRepository generatedReportRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final OperationalRecordRepository operationalRecordRepository;

    @Transactional(readOnly = true)
    public ReportManagementResponse getSummary(LocalDate fromDate, LocalDate toDate, UUID branchId) {
        LocalDate resolvedToDate = toDate != null ? toDate : LocalDate.now();
        LocalDate resolvedFromDate = fromDate != null ? fromDate : resolvedToDate.withDayOfMonth(1);
        validateDateRange(resolvedFromDate, resolvedToDate);

        List<OperationalRecord> feeRecords = loadOperationalRecords("FEE", resolvedFromDate, resolvedToDate, branchId);
        return ReportManagementResponse.builder()
                .fromDate(resolvedFromDate)
                .toDate(resolvedToDate)
                .summary(ReportManagementResponse.Summary.builder()
                        .totalStudents(studentRepository.countTotalStudents(branchId))
                        .totalTeachers(teacherRepository.countTotalTeachers(branchId))
                        .totalAdmissions(studentRepository.countAdmissionsBetween(branchId, resolvedFromDate, resolvedToDate))
                        .feesCollected(sum(feeRecords, true))
                        .pendingFees(sum(feeRecords, false))
                        .build())
                .categories(buildCategories())
                .recentReports(searchReports(null, null, null, branchId, null, null, null, 0, 8).getContent())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReportManagementResponse.ReportCategoryResponse> getCategories() {
        return buildCategories();
    }

    @Transactional(readOnly = true)
    public ReportManagementResponse.ReportListResponse searchReports(String category,
                                                                     String reportType,
                                                                     String format,
                                                                     UUID branchId,
                                                                     LocalDate fromDate,
                                                                     LocalDate toDate,
                                                                     String search,
                                                                     int page,
                                                                     int size) {
        LocalDateTime fromStart = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toExclusive = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "generatedOn"));
        Page<GeneratedReport> reportPage = generatedReportRepository.searchReports(
                normalize(category),
                normalize(reportType),
                normalize(format),
                branchId,
                fromDate,
                toDate,
                fromStart,
                toExclusive,
                buildSearchPattern(search),
                pageable
        );

        return ReportManagementResponse.ReportListResponse.builder()
                .content(reportPage.getContent().stream().map(this::toResponse).toList())
                .page(ReportManagementResponse.PageMeta.builder()
                        .pageNumber(reportPage.getNumber())
                        .pageSize(reportPage.getSize())
                        .totalElements(reportPage.getTotalElements())
                        .totalPages(reportPage.getTotalPages())
                        .first(reportPage.isFirst())
                        .last(reportPage.isLast())
                        .build())
                .build();
    }

    @Transactional
    public ReportManagementResponse.GenerateReportResponse generateReport(GenerateReportRequest request, UUID generatedByUserId) {
        ReportDefinition definition = resolveDefinition(request.getCategory(), request.getReportType());
        String format = normalizeFormat(request.getFormat(), definition.defaultFormat());
        LocalDate resolvedToDate = request.getToDate() != null ? request.getToDate() : LocalDate.now();
        LocalDate resolvedFromDate = request.getFromDate() != null ? request.getFromDate() : resolvedToDate.withDayOfMonth(1);
        validateDateRange(resolvedFromDate, resolvedToDate);

        Branch branch = resolveBranch(request.getBranchId());
        User generatedBy = userRepository.findByIdAndIsDeletedFalse(generatedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", generatedByUserId));

        GeneratedReport report = GeneratedReport.builder()
                .reportName(definition.label())
                .category(definition.category())
                .reportType(definition.key())
                .description(definition.description())
                .format(format)
                .status("READY")
                .branch(branch)
                .fromDate(resolvedFromDate)
                .toDate(resolvedToDate)
                .filtersJson(buildFiltersJson(request))
                .generatedByUser(generatedBy)
                .generatedOn(LocalDateTime.now())
                .storageProvider(STORAGE_PROVIDER_ON_DEMAND)
                .downloadUrl(null)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        report = generatedReportRepository.save(report);
        report.setDownloadUrl("/api/super-admin/reports/" + report.getId() + "/download");
        report = generatedReportRepository.save(report);

        return ReportManagementResponse.GenerateReportResponse.builder()
                .id(report.getId())
                .reportName(report.getReportName())
                .category(report.getCategory())
                .reportType(report.getReportType())
                .format(report.getFormat())
                .status(report.getStatus())
                .downloadUrl(report.getDownloadUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public ReportFile downloadReport(UUID reportId) {
        GeneratedReport report = generatedReportRepository.findByIdAndIsDeletedFalse(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", reportId));
        ReportDefinition definition = resolveDefinition(report.getCategory(), report.getReportType());
        List<List<String>> rows = buildReportRows(definition, report.getBranch() != null ? report.getBranch().getId() : null,
                report.getFromDate(), report.getToDate());

        String baseName = report.getReportType() + "-" + report.getGeneratedOn().toLocalDate();
        if (FORMAT_PDF.equalsIgnoreCase(report.getFormat())) {
            return ReportFile.builder()
                    .filename(baseName + ".pdf")
                    .contentType("application/pdf")
                    .content(buildSimplePdf(report.getReportName(), rows))
                    .build();
        }

        return ReportFile.builder()
                .filename(baseName + ".csv")
                .contentType("text/csv")
                .content(buildCsv(rows).getBytes(StandardCharsets.UTF_8))
                .build();
    }

    @Transactional
    public void deleteReport(UUID reportId) {
        GeneratedReport report = generatedReportRepository.findByIdAndIsDeletedFalse(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", reportId));
        report.softDelete();
        generatedReportRepository.save(report);
    }

    private List<List<String>> buildReportRows(ReportDefinition definition, UUID branchId, LocalDate fromDate, LocalDate toDate) {
        return switch (definition.category()) {
            case "student_reports" -> buildStudentReportRows(definition.key(), branchId);
            case "teacher_reports" -> buildTeacherReportRows(branchId);
            case "admission_reports" -> buildOperationalReportRows("ADMISSION", branchId, fromDate, toDate);
            case "attendance_reports" -> buildOperationalReportRows("ATTENDANCE", branchId, fromDate, toDate);
            case "academic_reports", "exam_reports" -> buildOperationalReportRows("TEST", branchId, fromDate, toDate);
            case "financial_reports" -> buildOperationalReportRows("FEE", branchId, fromDate, toDate);
            default -> throw new BadRequestException("Unsupported report category", "INVALID_REPORT_CATEGORY");
        };
    }

    private List<List<String>> buildStudentReportRows(String reportType, UUID branchId) {
        List<Student> students = studentRepository.searchStudents(null, null, branchId, null, null, PageRequest.of(0, 10000))
                .getContent();
        List<List<String>> rows = new java.util.ArrayList<>();
        rows.add(List.of("Student ID", "Name", "Standard", "Batch", "Gender", "Mobile", "Parent Name", "Parent Phone", "Status"));
        students.stream()
                .sorted(Comparator.comparing(Student::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(student -> rows.add(List.of(
                        value(student.getStudentId()),
                        value(student.getName()),
                        value(student.getStandard()),
                        value(student.getBatch()),
                        value(student.getGender()),
                        value(student.getMobile()),
                        value(student.getParentName()),
                        value(student.getParentPhone()),
                        Boolean.TRUE.equals(student.getIsActive()) ? "Active" : "Inactive"
                )));
        return rows;
    }

    private List<List<String>> buildTeacherReportRows(UUID branchId) {
        List<Teacher> teachers = branchId == null
                ? teacherRepository.findByIsDeletedFalseOrderByNameAsc(PageRequest.of(0, 10000)).getContent()
                : teacherRepository.findByBranch_IdAndIsDeletedFalseOrderByNameAsc(branchId, PageRequest.of(0, 10000)).getContent();
        List<List<String>> rows = new java.util.ArrayList<>();
        rows.add(List.of("Name", "Email", "Phone", "Subjects", "Qualification", "Experience Years", "Status"));
        teachers.forEach(teacher -> rows.add(List.of(
                value(teacher.getName()),
                value(teacher.getEmail()),
                value(teacher.getPhone()),
                String.join(", ", teacher.getSubjects()),
                value(teacher.getQualification()),
                teacher.getExperienceYears() != null ? teacher.getExperienceYears().toString() : "",
                Boolean.TRUE.equals(teacher.getIsActive()) ? "Active" : "Inactive"
        )));
        return rows;
    }

    private List<List<String>> buildOperationalReportRows(String module, UUID branchId, LocalDate fromDate, LocalDate toDate) {
        List<OperationalRecord> records = loadOperationalRecords(module, fromDate, toDate, branchId);
        List<List<String>> rows = new java.util.ArrayList<>();
        rows.add(List.of("Date", "Type", "Title", "Description", "Status", "Amount", "Paid Amount", "Pending Amount"));
        records.forEach(record -> rows.add(List.of(
                record.getEventDate() != null ? record.getEventDate().toString() : "",
                value(record.getType()),
                value(record.getTitle()),
                value(record.getDescription()),
                value(record.getStatus()),
                money(record.getAmount()),
                money(record.getPaidAmount()),
                money(record.getPendingAmount())
        )));
        return rows;
    }

    private List<OperationalRecord> loadOperationalRecords(String module, LocalDate fromDate, LocalDate toDate, UUID branchId) {
        LocalDate resolvedToDate = toDate != null ? toDate : LocalDate.now();
        LocalDate resolvedFromDate = fromDate != null ? fromDate : resolvedToDate.withDayOfMonth(1);
        Predicate<OperationalRecord> branchFilter = record -> branchId == null
                || (record.getBranch() != null && branchId.equals(record.getBranch().getId()));
        return operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        module, resolvedFromDate, resolvedToDate
                ).stream()
                .filter(branchFilter)
                .toList();
    }

    private List<ReportManagementResponse.ReportCategoryResponse> buildCategories() {
        return List.of(
                category("student_reports", "Student Reports", "View and download student related reports",
                        type("student_admission_report", "Student Admission Report", FORMAT_PDF),
                        type("student_attendance_report", "Student Attendance Report", FORMAT_CSV),
                        type("student_performance_report", "Student Performance Report", FORMAT_PDF),
                        type("student_result_report", "Student Result Report", FORMAT_CSV),
                        type("student_fee_report", "Student Fee Report", FORMAT_PDF),
                        type("student_personal_details_report", "Student Personal Details Report", FORMAT_CSV),
                        type("student_category_report", "Student Category Report", FORMAT_CSV),
                        type("student_age_report", "Student Age Report", FORMAT_PDF)),
                category("teacher_reports", "Teacher Reports", "View and download teacher related reports",
                        type("teacher_performance_report", "Teacher Performance Report", FORMAT_PDF),
                        type("teacher_attendance_report", "Teacher Attendance Report", FORMAT_CSV)),
                category("attendance_reports", "Attendance Reports", "View and download attendance reports",
                        type("student_attendance_summary", "Student Attendance Report", FORMAT_CSV)),
                category("academic_reports", "Academic Reports", "View and download academic performance reports",
                        type("student_result_report", "Student Result Report", FORMAT_PDF)),
                category("admission_reports", "Admission Reports", "View and download admission reports",
                        type("student_admission_report", "Student Admission Report", FORMAT_PDF)),
                category("exam_reports", "Exam Reports", "View and download exam related reports",
                        type("exam_analysis_report", "Exam Analysis Report", FORMAT_CSV)),
                category("financial_reports", "Financial Reports", "View and download financial reports",
                        type("fee_collection_report", "Fee Collection Report", FORMAT_CSV),
                        type("pending_fees_report", "Pending Fees Report", FORMAT_PDF))
        );
    }

    private ReportDefinition resolveDefinition(String category, String reportType) {
        String normalizedCategory = normalizeRequired(category, "Report category is required");
        String normalizedType = normalizeRequired(reportType, "Report type is required");
        return buildCategories().stream()
                .filter(item -> item.getKey().equals(normalizedCategory))
                .flatMap(item -> item.getReportTypes().stream()
                        .filter(type -> type.getKey().equals(normalizedType))
                        .map(type -> new ReportDefinition(item.getKey(), type.getKey(), type.getLabel(),
                                item.getDescription(), type.getDefaultFormat())))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unsupported report category or type", "INVALID_REPORT_TYPE"));
    }

    private ReportManagementResponse.ReportCategoryResponse category(String key,
                                                                    String label,
                                                                    String description,
                                                                    ReportManagementResponse.ReportTypeResponse... types) {
        return ReportManagementResponse.ReportCategoryResponse.builder()
                .key(key)
                .label(label)
                .description(description)
                .reportTypes(List.of(types))
                .build();
    }

    private ReportManagementResponse.ReportTypeResponse type(String key, String label, String defaultFormat) {
        return ReportManagementResponse.ReportTypeResponse.builder()
                .key(key)
                .label(label)
                .defaultFormat(defaultFormat)
                .build();
    }

    private ReportResponse toResponse(GeneratedReport report) {
        ReportDefinition definition = resolveDefinition(report.getCategory(), report.getReportType());
        return ReportResponse.builder()
                .id(report.getId())
                .reportName(report.getReportName())
                .category(report.getCategory())
                .categoryLabel(labelForCategory(report.getCategory()))
                .reportType(report.getReportType())
                .reportTypeLabel(definition.label())
                .description(report.getDescription())
                .generatedBy(report.getGeneratedByUser() != null ? report.getGeneratedByUser().getFullName() : null)
                .generatedOn(report.getGeneratedOn())
                .format(report.getFormat())
                .status(report.getStatus())
                .branchId(report.getBranch() != null ? report.getBranch().getId() : null)
                .branchName(report.getBranch() != null ? report.getBranch().getName() : "All Branches")
                .fromDate(report.getFromDate())
                .toDate(report.getToDate())
                .downloadUrl(report.getDownloadUrl())
                .build();
    }

    private String labelForCategory(String category) {
        return buildCategories().stream()
                .filter(item -> item.getKey().equals(category))
                .map(ReportManagementResponse.ReportCategoryResponse::getLabel)
                .findFirst()
                .orElse(category);
    }

    private Branch resolveBranch(UUID branchId) {
        if (branchId == null) {
            return null;
        }
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private BigDecimal sum(List<OperationalRecord> records, boolean paid) {
        return records.stream()
                .map(paid ? OperationalRecord::getPaidAmount : OperationalRecord::getPendingAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String normalizeFormat(String format, String defaultFormat) {
        String resolved = StringUtils.hasText(format) ? format.trim().toUpperCase(Locale.ENGLISH) : defaultFormat;
        if ("EXCEL".equals(resolved)) {
            return FORMAT_CSV;
        }
        if (!FORMAT_CSV.equals(resolved) && !FORMAT_PDF.equals(resolved)) {
            throw new BadRequestException("Supported report formats are CSV, EXCEL, and PDF", "INVALID_REPORT_FORMAT");
        }
        return resolved;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate cannot be after toDate", "INVALID_DATE_RANGE");
        }
    }

    private String buildFiltersJson(GenerateReportRequest request) {
        return "{"
                + "\"standard\":\"" + value(request.getStandard()) + "\","
                + "\"batch\":\"" + value(request.getBatch()) + "\""
                + "}";
    }

    private String buildCsv(List<List<String>> rows) {
        return rows.stream()
                .map(row -> row.stream().map(this::escapeCsv).collect(java.util.stream.Collectors.joining(",")))
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String escapeCsv(String value) {
        String resolved = value(value);
        if (resolved.contains(",") || resolved.contains("\"") || resolved.contains("\n")) {
            return "\"" + resolved.replace("\"", "\"\"") + "\"";
        }
        return resolved;
    }

    private byte[] buildSimplePdf(String title, List<List<String>> rows) {
        String text = title + "\n\n" + rows.stream()
                .limit(40)
                .map(row -> String.join(" | ", row))
                .collect(java.util.stream.Collectors.joining("\n"));
        String escaped = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        String stream = "BT /F1 9 Tf 40 780 Td 12 TL (" + escaped.replace("\n", ") Tj T* (") + ") Tj ET";
        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n"
                + "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "5 0 obj << /Length " + stream.getBytes(StandardCharsets.UTF_8).length + " >> stream\n"
                + stream + "\nendstream endobj\n"
                + "xref\n0 6\n0000000000 65535 f \n"
                + "trailer << /Root 1 0 R /Size 6 >>\nstartxref\n0\n%%EOF";
        return pdf.getBytes(StandardCharsets.UTF_8);
    }

    private String buildSearchPattern(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : "%" + normalized + "%";
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ENGLISH) : null;
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message, "VALIDATION_ERROR");
        }
        return value.trim().toLowerCase(Locale.ENGLISH);
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private String money(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private record ReportDefinition(String category, String key, String label, String description, String defaultFormat) {
    }
}
