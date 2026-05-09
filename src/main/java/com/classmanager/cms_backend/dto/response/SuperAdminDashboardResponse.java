package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminDashboardResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private UUID branchId;
    private String branchName;
    private List<BranchOption> branches;
    private Overview overview;
    private List<StudentGrowthPoint> studentGrowth;
    private List<FeeCollectionPoint> feeCollection;
    private AttendanceOverview attendanceOverview;
    private LeadConversionOverview leadConversionOverview;
    private List<ActivityItem> recentActivities;
    private FooterMetrics footerMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchOption {
        private UUID id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview {
        private MetricCard totalStudents;
        private MetricCard totalTeachers;
        private MetricCard totalAdmins;
        private MoneyMetric totalFeesCollected;
        private MoneyMetric pendingFees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricCard {
        private long total;
        private long changeThisMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoneyMetric {
        private BigDecimal total;
        private BigDecimal changeThisMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentGrowthPoint {
        private String month;
        private long totalStudents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeCollectionPoint {
        private String month;
        private BigDecimal feesCollected;
        private BigDecimal pendingFees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceOverview {
        private long present;
        private long leave;
        private long absent;
        private BigDecimal averageAttendancePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadConversionOverview {
        private List<LeadStage> stages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadStage {
        private String label;
        private long count;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String type;
        private String title;
        private String description;
        private String branchName;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FooterMetrics {
        private long totalClassesToday;
        private long teachersIn;
        private long studentsPresent;
        private long testsConducted;
        private long feedbacksReceived;
    }
}
