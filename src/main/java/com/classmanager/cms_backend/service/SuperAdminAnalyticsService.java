package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.SuperAdminAnalyticsResponse;
import com.classmanager.cms_backend.dto.response.SuperAdminDashboardResponse;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.OperationalRecord;
import com.classmanager.cms_backend.entity.Student;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.OperationalRecordRepository;
import com.classmanager.cms_backend.repository.StudentRepository;
import com.classmanager.cms_backend.repository.TeacherRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminAnalyticsService {

    private static final LocalDateTime ANALYTICS_BASELINE = LocalDateTime.of(1970, 1, 1, 0, 0);

    private static final String TAB_OVERVIEW = "overview";
    private static final String TAB_STUDENTS = "students";

    private static final String MODULE_ACTIVITY = "ACTIVITY";
    private static final String MODULE_LEAD = "LEAD";
    private static final String MODULE_FEE = "FEE";
    private static final String DROPPED_TYPE_DEACTIVATED = "STUDENT_DEACTIVATED";
    private static final String DROPPED_TYPE_DELETED = "STUDENT_DELETED";

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final OperationalRecordRepository operationalRecordRepository;

    @Transactional(readOnly = true)
    public SuperAdminAnalyticsResponse getAnalytics(String tab, LocalDate fromDate, LocalDate toDate, UUID branchId) {
        String resolvedTab = resolveTab(tab);
        LocalDate resolvedToDate = toDate != null ? toDate : LocalDate.now();
        LocalDate resolvedFromDate = fromDate != null ? fromDate : resolvedToDate.withDayOfYear(1);
        if (resolvedFromDate.isAfter(resolvedToDate)) {
            throw new BadRequestException("fromDate cannot be after toDate", "INVALID_DATE_RANGE");
        }

        Branch branch = resolveBranch(branchId);
        List<SuperAdminDashboardResponse.BranchOption> branches = loadBranches();
        List<Student> students = studentRepository.findAnalyticsStudents(branchId);
        Predicate<OperationalRecord> branchFilter = record -> branchId == null
                || (record.getBranch() != null && branchId.equals(record.getBranch().getId()));

        List<OperationalRecord> feeRecords = filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_FEE, resolvedFromDate, resolvedToDate
                ),
                branchFilter
        );
        List<OperationalRecord> leadRecords = filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_LEAD, resolvedFromDate, resolvedToDate
                ),
                branchFilter
        );
        List<OperationalRecord> activityRecords = filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_ACTIVITY, resolvedFromDate, resolvedToDate
                ),
                branchFilter
        );

        SuperAdminAnalyticsResponse.SuperAdminAnalyticsResponseBuilder responseBuilder = SuperAdminAnalyticsResponse.builder()
                .fromDate(resolvedFromDate)
                .toDate(resolvedToDate)
                .branchId(branch != null ? branch.getId() : null)
                .branchName(branch != null ? branch.getName() : "All Branches")
                .activeTab(resolvedTab)
                .branches(branches)
                .tabs(buildTabs());

        if (TAB_OVERVIEW.equals(resolvedTab)) {
            responseBuilder.overview(buildOverviewTab(branchId, resolvedFromDate, resolvedToDate, students, feeRecords, leadRecords));
        }

        if (TAB_STUDENTS.equals(resolvedTab)) {
            responseBuilder.students(buildStudentsTab(branchId, resolvedFromDate, resolvedToDate, students, activityRecords));
        }

        return responseBuilder.build();
    }

    private SuperAdminAnalyticsResponse.OverviewTab buildOverviewTab(UUID branchId,
                                                                    LocalDate fromDate,
                                                                    LocalDate toDate,
                                                                    List<Student> students,
                                                                    List<OperationalRecord> feeRecords,
                                                                    List<OperationalRecord> leadRecords) {
        return SuperAdminAnalyticsResponse.OverviewTab.builder()
                .cards(buildOverviewCards(branchId, fromDate, toDate, feeRecords))
                .studentGrowth(buildStudentGrowthComparison(branchId, toDate))
                .feeCollectionOverview(buildFeeCollectionOverview(feeRecords, fromDate, toDate))
                .leadConversionFunnel(buildLeadConversionFunnel(leadRecords))
                .admissionsOverview(buildAdmissionsOverview(students, fromDate, toDate, leadRecords))
                .build();
    }

    private SuperAdminAnalyticsResponse.StudentsTab buildStudentsTab(UUID branchId,
                                                                     LocalDate fromDate,
                                                                     LocalDate toDate,
                                                                     List<Student> students,
                                                                     List<OperationalRecord> activityRecords) {
        return SuperAdminAnalyticsResponse.StudentsTab.builder()
                .cards(buildStudentCards(branchId, fromDate, toDate, students, activityRecords))
                .studentGrowth(buildStudentGrowthComparison(branchId, toDate))
                .studentsByClass(buildDistribution(students, Student::getStandard))
                .studentsByGender(buildDistribution(students, student -> normalizeLabel(student.getGender(), "Unspecified")))
                .admissionsVsDropped(buildAdmissionsVsDropped(students, activityRecords, fromDate, toDate))
                .build();
    }

    private List<SuperAdminAnalyticsResponse.MetricCard> buildOverviewCards(UUID branchId,
                                                                            LocalDate fromDate,
                                                                            LocalDate toDate,
                                                                            List<OperationalRecord> feeRecords) {
        YearMonth currentMonth = YearMonth.from(toDate);
        LocalDateTime previousMonthEndExclusive = currentMonth.atDay(1).atStartOfDay();

        long totalStudents = studentRepository.countTotalStudents(branchId);
        long previousStudents = branchId == null
                ? studentRepository.countByCreatedAtBeforeAndIsDeletedFalse(previousMonthEndExclusive)
                : studentRepository.countByBranch_IdAndCreatedAtBeforeAndIsDeletedFalse(branchId, previousMonthEndExclusive);

        long totalTeachers = teacherRepository.countTotalTeachers(branchId);
        long previousTeachers = branchId == null
                ? teacherRepository.countByCreatedAtBetweenAndIsDeletedFalse(ANALYTICS_BASELINE, previousMonthEndExclusive)
                : teacherRepository.countByBranch_IdAndCreatedAtBetweenAndIsDeletedFalse(branchId, ANALYTICS_BASELINE, previousMonthEndExclusive);

        long totalAdmins = branchId == null
                ? userRepository.countByRoleName(UserRole.ADMIN.name())
                : userRepository.countByRoleNameAndBranchId(UserRole.ADMIN.name(), branchId);
        long previousAdmins = branchId == null
                ? userRepository.countByRoleNameCreatedBetween(UserRole.ADMIN.name(), ANALYTICS_BASELINE, previousMonthEndExclusive)
                : userRepository.countByRoleNameAndBranchIdCreatedBetween(UserRole.ADMIN.name(), branchId, ANALYTICS_BASELINE, previousMonthEndExclusive);

        BigDecimal currentFees = sumAmounts(feeRecords, OperationalRecord::getPaidAmount);
        BigDecimal currentPending = sumAmounts(feeRecords, OperationalRecord::getPendingAmount);
        LocalDate previousFromDate = fromDate.minusDays(toDate.toEpochDay() - fromDate.toEpochDay() + 1L);
        LocalDate previousToDate = fromDate.minusDays(1);
        List<OperationalRecord> previousFeeRecords = previousToDate.isBefore(previousFromDate)
                ? List.of()
                : filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_FEE, previousFromDate, previousToDate
                ),
                record -> branchId == null || (record.getBranch() != null && branchId.equals(record.getBranch().getId()))
        );

        BigDecimal previousFees = sumAmounts(previousFeeRecords, OperationalRecord::getPaidAmount);
        BigDecimal previousPending = sumAmounts(previousFeeRecords, OperationalRecord::getPendingAmount);

        return List.of(
                metricCountCard("totalStudents", "Total Students", totalStudents, previousStudents, "vs last month"),
                metricCountCard("totalTeachers", "Total Teachers", totalTeachers, previousTeachers, "vs last month"),
                metricCountCard("totalAdmins", "Total Admins", totalAdmins, previousAdmins, "vs last month"),
                metricAmountCard("feesCollected", "Fees Collected", currentFees, previousFees, "vs previous period"),
                metricAmountCard("pendingFees", "Pending Fees", currentPending, previousPending, "vs previous period")
        );
    }

    private List<SuperAdminAnalyticsResponse.MetricCard> buildStudentCards(UUID branchId,
                                                                           LocalDate fromDate,
                                                                           LocalDate toDate,
                                                                           List<Student> students,
                                                                           List<OperationalRecord> activityRecords) {
        long totalStudents = students.size();
        long activeStudents = students.stream().filter(student -> Boolean.TRUE.equals(student.getIsActive())).count();
        long inactiveStudents = students.stream().filter(student -> Boolean.FALSE.equals(student.getIsActive())).count();

        long newAdmissions = studentRepository.countAdmissionsBetween(branchId, fromDate, toDate);

        long droppedStudents = activityRecords.stream()
                .filter(this::isDroppedStudentEvent)
                .count();

        long daysInRange = toDate.toEpochDay() - fromDate.toEpochDay() + 1L;
        LocalDate previousStart = fromDate.minusDays(daysInRange);
        LocalDate previousEnd = fromDate.minusDays(1);
        long previousNewAdmissions = studentRepository.countAdmissionsBetween(branchId, previousStart, previousEnd);
        long previousDroppedStudents = loadPreviousDroppedStudents(branchId, fromDate.minusDays(daysInRange), fromDate.minusDays(1));

        return List.of(
                metricCountCard("totalStudents", "Total Students", totalStudents, previousMonthStudentTotal(branchId, toDate), "vs last month"),
                metricShareCard("activeStudents", "Active Students", activeStudents, totalStudents),
                metricShareCard("inactiveStudents", "Inactive Students", inactiveStudents, totalStudents),
                metricCountCard("newAdmissions", "New Admissions", newAdmissions, previousNewAdmissions, "vs previous period"),
                metricCountCard("droppedStudents", "Dropped Students", droppedStudents, previousDroppedStudents, "vs previous period")
        );
    }

    private List<SuperAdminAnalyticsResponse.ComparisonPoint> buildStudentGrowthComparison(UUID branchId, LocalDate toDate) {
        List<SuperAdminAnalyticsResponse.ComparisonPoint> points = new ArrayList<>();
        int year = toDate.getYear();
        for (int monthValue = 1; monthValue <= 12; monthValue++) {
            YearMonth currentMonth = YearMonth.of(year, monthValue);
            YearMonth previousMonth = currentMonth.minusYears(1);
            LocalDateTime currentBoundary = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
            LocalDateTime previousBoundary = previousMonth.plusMonths(1).atDay(1).atStartOfDay();

            long currentValue = branchId == null
                    ? studentRepository.countByCreatedAtBeforeAndIsDeletedFalse(currentBoundary)
                    : studentRepository.countByBranch_IdAndCreatedAtBeforeAndIsDeletedFalse(branchId, currentBoundary);
            long previousValue = branchId == null
                    ? studentRepository.countByCreatedAtBeforeAndIsDeletedFalse(previousBoundary)
                    : studentRepository.countByBranch_IdAndCreatedAtBeforeAndIsDeletedFalse(branchId, previousBoundary);

            points.add(SuperAdminAnalyticsResponse.ComparisonPoint.builder()
                    .month(shortMonth(currentMonth))
                    .currentValue(currentValue)
                    .previousValue(previousValue)
                    .build());
        }
        return points;
    }

    private List<SuperAdminAnalyticsResponse.FeeCollectionPoint> buildFeeCollectionOverview(List<OperationalRecord> feeRecords,
                                                                                            LocalDate fromDate,
                                                                                            LocalDate toDate) {
        Map<YearMonth, List<OperationalRecord>> grouped = feeRecords.stream()
                .filter(record -> record.getEventDate() != null)
                .collect(Collectors.groupingBy(record -> YearMonth.from(record.getEventDate())));

        List<SuperAdminAnalyticsResponse.FeeCollectionPoint> points = new ArrayList<>();
        YearMonth cursor = YearMonth.from(fromDate);
        YearMonth endMonth = YearMonth.from(toDate);
        while (!cursor.isAfter(endMonth)) {
            List<OperationalRecord> records = grouped.getOrDefault(cursor, List.of());
            points.add(SuperAdminAnalyticsResponse.FeeCollectionPoint.builder()
                    .month(shortMonth(cursor))
                    .feesCollected(sumAmounts(records, OperationalRecord::getPaidAmount))
                    .pendingFees(sumAmounts(records, OperationalRecord::getPendingAmount))
                    .build());
            cursor = cursor.plusMonths(1);
        }
        return points;
    }

    private SuperAdminAnalyticsResponse.LeadConversionFunnel buildLeadConversionFunnel(List<OperationalRecord> leadRecords) {
        long totalLeads = leadRecords.size();
        long interested = leadRecords.stream().filter(this::isInterestedLeadStatus).count();
        long converted = leadRecords.stream().filter(record -> hasStatus(record, "CONVERTED")).count();
        long admissions = leadRecords.stream().filter(record -> hasStatus(record, "ADMISSION_COMPLETED")).count();

        return SuperAdminAnalyticsResponse.LeadConversionFunnel.builder()
                .stages(List.of(
                        buildFunnelStage("Total Leads", totalLeads, totalLeads),
                        buildFunnelStage("Interested", interested, totalLeads),
                        buildFunnelStage("Converted", converted, totalLeads),
                        buildFunnelStage("Admissions", admissions, totalLeads)
                ))
                .build();
    }

    private SuperAdminAnalyticsResponse.AdmissionOverview buildAdmissionsOverview(List<Student> students,
                                                                                  LocalDate fromDate,
                                                                                  LocalDate toDate,
                                                                                  List<OperationalRecord> leadRecords) {
        List<Student> admissions = students.stream()
                .filter(student -> Boolean.TRUE.equals(student.getIsAdmissionFinal()))
                .filter(student -> {
                    LocalDate admissionDate = resolveAdmissionDate(student);
                    return admissionDate != null
                            && !admissionDate.isBefore(fromDate)
                            && !admissionDate.isAfter(toDate);
                })
                .toList();

        Map<String, Long> branchCounts = admissions.stream()
                .collect(Collectors.groupingBy(student -> student.getBranch().getName(), LinkedHashMap::new, Collectors.counting()));

        long totalAdmissions = admissions.size();
        long totalLeads = leadRecords.size();
        BigDecimal conversionRate = percentage(totalAdmissions, totalLeads);

        List<SuperAdminAnalyticsResponse.DistributionSegment> branches = branchCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> buildSegment(entry.getKey(), entry.getValue(), totalAdmissions))
                .toList();

        return SuperAdminAnalyticsResponse.AdmissionOverview.builder()
                .totalAdmissions(totalAdmissions)
                .branches(branches)
                .conversionRate(conversionRate)
                .inquiryToAdmissionRate(conversionRate)
                .build();
    }

    private SuperAdminAnalyticsResponse.Distribution buildDistribution(List<Student> students,
                                                                      java.util.function.Function<Student, String> classifier) {
        Map<String, Long> counts = students.stream()
                .map(classifier)
                .collect(Collectors.groupingBy(value -> normalizeLabel(value, "Unspecified"),
                        LinkedHashMap::new,
                        Collectors.counting()));

        long total = students.size();
        List<SuperAdminAnalyticsResponse.DistributionSegment> segments = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> buildSegment(entry.getKey(), entry.getValue(), total))
                .toList();

        return SuperAdminAnalyticsResponse.Distribution.builder()
                .total(total)
                .segments(segments)
                .build();
    }

    private List<SuperAdminAnalyticsResponse.AdmissionDropPoint> buildAdmissionsVsDropped(List<Student> students,
                                                                                           List<OperationalRecord> activityRecords,
                                                                                           LocalDate fromDate,
                                                                                           LocalDate toDate) {
        Map<YearMonth, Long> admissionsByMonth = students.stream()
                .filter(student -> Boolean.TRUE.equals(student.getIsAdmissionFinal()))
                .map(this::resolveAdmissionDate)
                .filter(date -> date != null && !date.isBefore(fromDate) && !date.isAfter(toDate))
                .collect(Collectors.groupingBy(YearMonth::from, Collectors.counting()));

        Map<YearMonth, Long> droppedByMonth = activityRecords.stream()
                .filter(this::isDroppedStudentEvent)
                .map(record -> record.getEventDate() != null ? record.getEventDate() : record.getCreatedAt().toLocalDate())
                .filter(date -> date != null && !date.isBefore(fromDate) && !date.isAfter(toDate))
                .collect(Collectors.groupingBy(YearMonth::from, Collectors.counting()));

        List<SuperAdminAnalyticsResponse.AdmissionDropPoint> points = new ArrayList<>();
        YearMonth cursor = YearMonth.from(fromDate);
        YearMonth endMonth = YearMonth.from(toDate);
        while (!cursor.isAfter(endMonth)) {
            points.add(SuperAdminAnalyticsResponse.AdmissionDropPoint.builder()
                    .month(shortMonth(cursor))
                    .newAdmissions(admissionsByMonth.getOrDefault(cursor, 0L))
                    .droppedStudents(droppedByMonth.getOrDefault(cursor, 0L))
                    .build());
            cursor = cursor.plusMonths(1);
        }
        return points;
    }

    private long previousMonthStudentTotal(UUID branchId, LocalDate toDate) {
        LocalDateTime previousMonthEndExclusive = YearMonth.from(toDate).atDay(1).atStartOfDay();
        return branchId == null
                ? studentRepository.countByCreatedAtBeforeAndIsDeletedFalse(previousMonthEndExclusive)
                : studentRepository.countByBranch_IdAndCreatedAtBeforeAndIsDeletedFalse(branchId, previousMonthEndExclusive);
    }

    private long loadPreviousDroppedStudents(UUID branchId, LocalDate fromDate, LocalDate toDate) {
        if (toDate.isBefore(fromDate)) {
            return 0;
        }
        return filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_ACTIVITY, fromDate, toDate
                ),
                record -> branchId == null || (record.getBranch() != null && branchId.equals(record.getBranch().getId()))
        ).stream().filter(this::isDroppedStudentEvent).count();
    }

    private String resolveTab(String tab) {
        String resolved = tab == null ? TAB_OVERVIEW : tab.trim().toLowerCase(Locale.ENGLISH);
        if (!TAB_OVERVIEW.equals(resolved) && !TAB_STUDENTS.equals(resolved)) {
            throw new BadRequestException("Supported analytics tabs are overview and students", "INVALID_ANALYTICS_TAB");
        }
        return resolved;
    }

    private Branch resolveBranch(UUID branchId) {
        if (branchId == null) {
            return null;
        }
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private List<SuperAdminDashboardResponse.BranchOption> loadBranches() {
        return branchRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc().stream()
                .map(item -> SuperAdminDashboardResponse.BranchOption.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .build())
                .toList();
    }

    private List<SuperAdminAnalyticsResponse.TabOption> buildTabs() {
        return List.of(
                SuperAdminAnalyticsResponse.TabOption.builder().key(TAB_OVERVIEW).label("Overview").build(),
                SuperAdminAnalyticsResponse.TabOption.builder().key(TAB_STUDENTS).label("Students").build(),
                SuperAdminAnalyticsResponse.TabOption.builder().key("teachers").label("Teachers").build(),
                SuperAdminAnalyticsResponse.TabOption.builder().key("attendance").label("Attendance").build(),
                SuperAdminAnalyticsResponse.TabOption.builder().key("academics").label("Academics").build(),
                SuperAdminAnalyticsResponse.TabOption.builder().key("finance").label("Finance").build(),
                SuperAdminAnalyticsResponse.TabOption.builder().key("admissions").label("Admissions").build(),
                SuperAdminAnalyticsResponse.TabOption.builder().key("performance").label("Performance").build(),
                SuperAdminAnalyticsResponse.TabOption.builder().key("feedback").label("Feedback").build()
        );
    }

    private List<OperationalRecord> filterRecords(List<OperationalRecord> records, Predicate<OperationalRecord> predicate) {
        return records.stream()
                .filter(record -> !record.isDeleted())
                .filter(predicate)
                .sorted(Comparator.comparing(OperationalRecord::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(OperationalRecord::getCreatedAt))
                .toList();
    }

    private SuperAdminAnalyticsResponse.MetricCard metricCountCard(String key,
                                                                  String label,
                                                                  long total,
                                                                  long comparisonTotal,
                                                                  String comparisonLabel) {
        BigDecimal changePercentage = percentageChange(total, comparisonTotal);
        return SuperAdminAnalyticsResponse.MetricCard.builder()
                .key(key)
                .label(label)
                .totalCount(total)
                .changePercentage(changePercentage.abs())
                .comparisonLabel(comparisonLabel)
                .trendDirection(trendDirection(changePercentage))
                .build();
    }

    private SuperAdminAnalyticsResponse.MetricCard metricAmountCard(String key,
                                                                   String label,
                                                                   BigDecimal total,
                                                                   BigDecimal comparisonTotal,
                                                                   String comparisonLabel) {
        BigDecimal changePercentage = percentageChange(total, comparisonTotal);
        return SuperAdminAnalyticsResponse.MetricCard.builder()
                .key(key)
                .label(label)
                .totalAmount(total)
                .changePercentage(changePercentage.abs())
                .comparisonLabel(comparisonLabel)
                .trendDirection(trendDirection(changePercentage))
                .build();
    }

    private SuperAdminAnalyticsResponse.MetricCard metricShareCard(String key, String label, long total, long overallTotal) {
        return SuperAdminAnalyticsResponse.MetricCard.builder()
                .key(key)
                .label(label)
                .totalCount(total)
                .sharePercentage(percentage(total, overallTotal))
                .shareLabel("of total")
                .trendDirection("neutral")
                .build();
    }

    private SuperAdminAnalyticsResponse.FunnelStage buildFunnelStage(String label, long count, long total) {
        return SuperAdminAnalyticsResponse.FunnelStage.builder()
                .label(label)
                .count(count)
                .percentage(percentage(count, total))
                .build();
    }

    private SuperAdminAnalyticsResponse.DistributionSegment buildSegment(String label, long count, long total) {
        return SuperAdminAnalyticsResponse.DistributionSegment.builder()
                .label(label)
                .count(count)
                .percentage(percentage(count, total))
                .build();
    }

    private BigDecimal sumAmounts(List<OperationalRecord> records,
                                  java.util.function.Function<OperationalRecord, BigDecimal> extractor) {
        return records.stream()
                .map(extractor)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate resolveAdmissionDate(Student student) {
        if (student.getAdmissionDate() != null) {
            return student.getAdmissionDate();
        }
        return student.getCreatedAt() != null ? student.getCreatedAt().toLocalDate() : null;
    }

    private boolean isDroppedStudentEvent(OperationalRecord record) {
        return record.getType() != null
                && (DROPPED_TYPE_DEACTIVATED.equalsIgnoreCase(record.getType())
                || DROPPED_TYPE_DELETED.equalsIgnoreCase(record.getType()));
    }

    private boolean hasStatus(OperationalRecord record, String status) {
        return record.getStatus() != null && record.getStatus().equalsIgnoreCase(status);
    }

    private boolean isInterestedLeadStatus(OperationalRecord record) {
        return hasStatus(record, "INTERESTED")
                || hasStatus(record, "CONTACTED")
                || hasStatus(record, "IN_FOLLOW_UP");
    }

    private String shortMonth(YearMonth month) {
        return month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    private String normalizeLabel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private BigDecimal percentage(long value, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentageChange(long current, long previous) {
        return percentageChange(BigDecimal.valueOf(current), BigDecimal.valueOf(previous));
    }

    private BigDecimal percentageChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current == null || current.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            return BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    private String trendDirection(BigDecimal changePercentage) {
        int sign = changePercentage.compareTo(BigDecimal.ZERO);
        if (sign > 0) {
            return "up";
        }
        if (sign < 0) {
            return "down";
        }
        return "neutral";
    }
}
