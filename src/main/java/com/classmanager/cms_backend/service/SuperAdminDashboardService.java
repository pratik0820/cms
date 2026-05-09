package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.SuperAdminDashboardResponse;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.OperationalRecord;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.OperationalRecordRepository;
import com.classmanager.cms_backend.repository.StudentRepository;
import com.classmanager.cms_backend.repository.TeacherRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminDashboardService {

    public static final String MODULE_LEAD = "LEAD";
    public static final String MODULE_ADMISSION = "ADMISSION";
    public static final String MODULE_FEE = "FEE";
    public static final String MODULE_ATTENDANCE = "ATTENDANCE";
    public static final String MODULE_CLASS_SESSION = "CLASS_SESSION";
    public static final String MODULE_TEST = "TEST";
    public static final String MODULE_FEEDBACK = "FEEDBACK";
    public static final String MODULE_ACTIVITY = "ACTIVITY";

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final OperationalRecordRepository operationalRecordRepository;

    public SuperAdminDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, UUID branchId) {
        LocalDate resolvedToDate = toDate != null ? toDate : LocalDate.now();
        LocalDate resolvedFromDate = fromDate != null ? fromDate : resolvedToDate.withDayOfMonth(1);

        Branch branch = resolveBranch(branchId);
        List<Branch> branches = branchRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
        Predicate<OperationalRecord> branchFilter = record -> branchId == null
                || (record.getBranch() != null && branchId.equals(record.getBranch().getId()));

        List<OperationalRecord> feeRecords = filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_FEE, resolvedFromDate, resolvedToDate
                ),
                branchFilter
        );
        List<OperationalRecord> attendanceRecords = filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_ATTENDANCE, resolvedFromDate, resolvedToDate
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
                operationalRecordRepository.findTop10ByModuleAndIsDeletedFalseOrderByCreatedAtDesc(MODULE_ACTIVITY),
                branchFilter
        );
        List<OperationalRecord> classSessionRecords = filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_CLASS_SESSION, resolvedFromDate, resolvedToDate
                ),
                branchFilter
        );
        List<OperationalRecord> testRecords = filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_TEST, resolvedFromDate, resolvedToDate
                ),
                branchFilter
        );
        List<OperationalRecord> feedbackRecords = filterRecords(
                operationalRecordRepository.findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
                        MODULE_FEEDBACK, resolvedFromDate, resolvedToDate
                ),
                branchFilter
        );

        return SuperAdminDashboardResponse.builder()
                .fromDate(resolvedFromDate)
                .toDate(resolvedToDate)
                .branchId(branch != null ? branch.getId() : null)
                .branchName(branch != null ? branch.getName() : "All Branches")
                .branches(branches.stream()
                        .map(item -> SuperAdminDashboardResponse.BranchOption.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .build())
                        .toList())
                .overview(buildOverview(branchId, feeRecords))
                .studentGrowth(buildStudentGrowth(branchId, resolvedToDate))
                .feeCollection(buildFeeCollection(feeRecords, resolvedFromDate, resolvedToDate))
                .attendanceOverview(buildAttendanceOverview(attendanceRecords))
                .leadConversionOverview(buildLeadOverview(leadRecords))
                .recentActivities(buildActivities(activityRecords))
                .footerMetrics(buildFooterMetrics(branchId, resolvedToDate, classSessionRecords, attendanceRecords, testRecords, feedbackRecords))
                .build();
    }

    private SuperAdminDashboardResponse.Overview buildOverview(UUID branchId, List<OperationalRecord> feeRecords) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        long totalStudents = branchId == null
                ? studentRepository.countByIsDeletedFalse()
                : studentRepository.countByBranch_IdAndIsDeletedFalse(branchId);
        long studentChange = branchId == null
                ? studentRepository.countByCreatedAtBetweenAndIsDeletedFalse(monthStart, nextMonthStart)
                : studentRepository.countByBranch_IdAndCreatedAtBetweenAndIsDeletedFalse(branchId, monthStart, nextMonthStart);

        long totalTeachers = branchId == null
                ? teacherRepository.countByIsDeletedFalse()
                : teacherRepository.countByBranch_IdAndIsDeletedFalse(branchId);
        long teacherChange = branchId == null
                ? teacherRepository.countByCreatedAtBetweenAndIsDeletedFalse(monthStart, nextMonthStart)
                : teacherRepository.countByBranch_IdAndCreatedAtBetweenAndIsDeletedFalse(branchId, monthStart, nextMonthStart);

        long totalAdmins = branchId == null
                ? userRepository.countByRoleName(UserRole.ADMIN.name())
                : userRepository.countByRoleNameAndBranchId(UserRole.ADMIN.name(), branchId);
        long adminChange = branchId == null
                ? userRepository.countByRoleNameCreatedBetween(UserRole.ADMIN.name(), monthStart, nextMonthStart)
                : userRepository.countByRoleNameAndBranchIdCreatedBetween(UserRole.ADMIN.name(), branchId, monthStart, nextMonthStart);

        BigDecimal totalFeesCollected = feeRecords.stream()
                .map(OperationalRecord::getPaidAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingFees = feeRecords.stream()
                .map(OperationalRecord::getPendingAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal feesThisMonth = feeRecords.stream()
                .filter(record -> isWithinMonth(record.getEventDate(), YearMonth.now()))
                .map(OperationalRecord::getPaidAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingThisMonth = feeRecords.stream()
                .filter(record -> isWithinMonth(record.getEventDate(), YearMonth.now()))
                .map(OperationalRecord::getPendingAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SuperAdminDashboardResponse.Overview.builder()
                .totalStudents(SuperAdminDashboardResponse.MetricCard.builder()
                        .total(totalStudents)
                        .changeThisMonth(studentChange)
                        .build())
                .totalTeachers(SuperAdminDashboardResponse.MetricCard.builder()
                        .total(totalTeachers)
                        .changeThisMonth(teacherChange)
                        .build())
                .totalAdmins(SuperAdminDashboardResponse.MetricCard.builder()
                        .total(totalAdmins)
                        .changeThisMonth(adminChange)
                        .build())
                .totalFeesCollected(SuperAdminDashboardResponse.MoneyMetric.builder()
                        .total(totalFeesCollected)
                        .changeThisMonth(feesThisMonth)
                        .build())
                .pendingFees(SuperAdminDashboardResponse.MoneyMetric.builder()
                        .total(pendingFees)
                        .changeThisMonth(pendingThisMonth)
                        .build())
                .build();
    }

    private List<SuperAdminDashboardResponse.StudentGrowthPoint> buildStudentGrowth(UUID branchId, LocalDate toDate) {
        List<SuperAdminDashboardResponse.StudentGrowthPoint> points = new ArrayList<>();
        YearMonth lastMonth = YearMonth.from(toDate);
        for (int i = 11; i >= 0; i--) {
            YearMonth month = lastMonth.minusMonths(i);
            LocalDateTime monthStart = month.atDay(1).atStartOfDay();
            LocalDateTime nextMonthStart = month.plusMonths(1).atDay(1).atStartOfDay();
            long count = branchId == null
                    ? studentRepository.countByCreatedAtBeforeAndIsDeletedFalse(nextMonthStart)
                    : studentRepository.countByBranch_IdAndCreatedAtBeforeAndIsDeletedFalse(branchId, nextMonthStart);
            points.add(SuperAdminDashboardResponse.StudentGrowthPoint.builder()
                    .month(shortMonth(month))
                    .totalStudents(count)
                    .build());
        }
        return points;
    }

    private List<SuperAdminDashboardResponse.FeeCollectionPoint> buildFeeCollection(List<OperationalRecord> feeRecords,
                                                                                    LocalDate fromDate,
                                                                                    LocalDate toDate) {
        Map<YearMonth, List<OperationalRecord>> grouped = feeRecords.stream()
                .filter(record -> record.getEventDate() != null)
                .collect(Collectors.groupingBy(record -> YearMonth.from(record.getEventDate())));

        List<SuperAdminDashboardResponse.FeeCollectionPoint> points = new ArrayList<>();
        YearMonth startMonth = YearMonth.from(fromDate);
        YearMonth endMonth = YearMonth.from(toDate);
        YearMonth cursor = startMonth;
        while (!cursor.isAfter(endMonth)) {
            List<OperationalRecord> records = grouped.getOrDefault(cursor, List.of());
            BigDecimal collected = records.stream()
                    .map(OperationalRecord::getPaidAmount)
                    .filter(value -> value != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pending = records.stream()
                    .map(OperationalRecord::getPendingAmount)
                    .filter(value -> value != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            points.add(SuperAdminDashboardResponse.FeeCollectionPoint.builder()
                    .month(shortMonth(cursor))
                    .feesCollected(collected)
                    .pendingFees(pending)
                    .build());
            cursor = cursor.plusMonths(1);
        }
        return points;
    }

    private SuperAdminDashboardResponse.AttendanceOverview buildAttendanceOverview(List<OperationalRecord> attendanceRecords) {
        long present = attendanceRecords.stream().filter(record -> hasStatus(record, "PRESENT")).count();
        long leave = attendanceRecords.stream().filter(record -> hasStatus(record, "LEAVE")).count();
        long absent = attendanceRecords.stream().filter(record -> hasStatus(record, "ABSENT")).count();
        long total = present + leave + absent;

        BigDecimal percentage = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf((present * 100.0) / total).setScale(2, RoundingMode.HALF_UP);

        return SuperAdminDashboardResponse.AttendanceOverview.builder()
                .present(present)
                .leave(leave)
                .absent(absent)
                .averageAttendancePercentage(percentage)
                .build();
    }

    private SuperAdminDashboardResponse.LeadConversionOverview buildLeadOverview(List<OperationalRecord> leadRecords) {
        long totalLeads = leadRecords.size();
        long interested = leadRecords.stream().filter(record -> hasStatus(record, "INTERESTED")).count();
        long converted = leadRecords.stream().filter(record -> hasStatus(record, "CONVERTED")).count();
        long admissions = leadRecords.stream().filter(record -> hasStatus(record, "ADMISSION_COMPLETED")).count();

        return SuperAdminDashboardResponse.LeadConversionOverview.builder()
                .stages(List.of(
                        buildLeadStage("Total Leads", totalLeads, totalLeads),
                        buildLeadStage("Interested", interested, totalLeads),
                        buildLeadStage("Converted", converted, totalLeads),
                        buildLeadStage("Admission", admissions, totalLeads)
                ))
                .build();
    }

    private List<SuperAdminDashboardResponse.ActivityItem> buildActivities(List<OperationalRecord> activityRecords) {
        return activityRecords.stream()
                .sorted(Comparator.comparing(OperationalRecord::getCreatedAt).reversed())
                .limit(6)
                .map(record -> SuperAdminDashboardResponse.ActivityItem.builder()
                        .type(record.getType())
                        .title(record.getTitle())
                        .description(record.getDescription())
                        .branchName(record.getBranch() != null ? record.getBranch().getName() : null)
                        .createdAt(record.getCreatedAt())
                        .build())
                .toList();
    }

    private SuperAdminDashboardResponse.FooterMetrics buildFooterMetrics(UUID branchId,
                                                                        LocalDate toDate,
                                                                        List<OperationalRecord> classSessions,
                                                                        List<OperationalRecord> attendanceRecords,
                                                                        List<OperationalRecord> testRecords,
                                                                        List<OperationalRecord> feedbackRecords) {
        long totalClassesToday = classSessions.stream()
                .filter(record -> toDate.equals(record.getEventDate()))
                .count();
        long teachersIn = classSessions.stream()
                .filter(record -> toDate.equals(record.getEventDate()))
                .filter(record -> hasStatus(record, "IN_PROGRESS"))
                .count();
        long studentsPresent = attendanceRecords.stream()
                .filter(record -> toDate.equals(record.getEventDate()))
                .filter(record -> hasStatus(record, "PRESENT"))
                .count();
        long testsConducted = testRecords.stream()
                .filter(record -> toDate.equals(record.getEventDate()))
                .count();
        long feedbacksReceived = feedbackRecords.stream()
                .filter(record -> toDate.equals(record.getEventDate()))
                .count();

        return SuperAdminDashboardResponse.FooterMetrics.builder()
                .totalClassesToday(totalClassesToday)
                .teachersIn(teachersIn)
                .studentsPresent(studentsPresent)
                .testsConducted(testsConducted)
                .feedbacksReceived(feedbacksReceived)
                .build();
    }

    private Branch resolveBranch(UUID branchId) {
        if (branchId == null) {
            return null;
        }
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private List<OperationalRecord> filterRecords(List<OperationalRecord> records, Predicate<OperationalRecord> predicate) {
        return records.stream().filter(predicate).toList();
    }

    private String shortMonth(YearMonth month) {
        return month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    private boolean isWithinMonth(LocalDate date, YearMonth month) {
        return date != null && YearMonth.from(date).equals(month);
    }

    private boolean hasStatus(OperationalRecord record, String expectedStatus) {
        return record.getStatus() != null && record.getStatus().equalsIgnoreCase(expectedStatus);
    }

    private SuperAdminDashboardResponse.LeadStage buildLeadStage(String label, long count, long totalLeads) {
        BigDecimal percentage = totalLeads == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf((count * 100.0) / totalLeads).setScale(2, RoundingMode.HALF_UP);
        return SuperAdminDashboardResponse.LeadStage.builder()
                .label(label)
                .count(count)
                .percentage(percentage)
                .build();
    }
}
