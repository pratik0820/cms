package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminAnalyticsResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private UUID branchId;
    private String branchName;
    private String activeTab;
    private List<SuperAdminDashboardResponse.BranchOption> branches;
    private List<TabOption> tabs;
    private OverviewTab overview;
    private StudentsTab students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabOption {
        private String key;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewTab {
        private List<MetricCard> cards;
        private List<ComparisonPoint> studentGrowth;
        private List<FeeCollectionPoint> feeCollectionOverview;
        private LeadConversionFunnel leadConversionFunnel;
        private AdmissionOverview admissionsOverview;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentsTab {
        private List<MetricCard> cards;
        private List<ComparisonPoint> studentGrowth;
        private Distribution studentsByClass;
        private Distribution studentsByGender;
        private List<AdmissionDropPoint> admissionsVsDropped;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricCard {
        private String key;
        private String label;
        private Long totalCount;
        private BigDecimal totalAmount;
        private BigDecimal changePercentage;
        private String comparisonLabel;
        private String trendDirection;
        private BigDecimal sharePercentage;
        private String shareLabel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonPoint {
        private String month;
        private long currentValue;
        private long previousValue;
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
    public static class LeadConversionFunnel {
        private List<FunnelStage> stages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunnelStage {
        private String label;
        private long count;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdmissionOverview {
        private long totalAdmissions;
        private List<DistributionSegment> branches;
        private BigDecimal conversionRate;
        private BigDecimal inquiryToAdmissionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Distribution {
        private long total;
        private List<DistributionSegment> segments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributionSegment {
        private String label;
        private long count;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdmissionDropPoint {
        private String month;
        private long newAdmissions;
        private long droppedStudents;
    }
}
