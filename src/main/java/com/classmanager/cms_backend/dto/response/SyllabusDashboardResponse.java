package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SyllabusDashboardResponse {
    private OverviewStats stats;
    private List<MonthlyTrend> trends;
    private StatusDistribution statusDistribution;
    private List<ClassCompletion> classCompletions;
    private List<SubjectCompletion> subjectCompletions;
    private List<TeacherPerformance> teacherPerformances;
    private List<Deadline> upcomingDeadlines;

    @Data
    @Builder
    public static class OverviewStats {
        private int totalSubjects;
        private int totalClasses;
        private int totalTeachers;
        private double overallCompletionPercentage;
        private int pendingTopics;
    }

    @Data
    @Builder
    public static class MonthlyTrend {
        private String month; // e.g., "Jan", "Feb"
        private double value; // completion percentage for that month
    }

    @Data
    @Builder
    public static class StatusDistribution {
        private double completedPercentage;
        private int completedCount;
        private double inProgressPercentage;
        private int inProgressCount;
        private double notStartedPercentage;
        private int notStartedCount;
    }

    @Data
    @Builder
    public static class ClassCompletion {
        private String className;
        private double completionPercentage;
        private String status;
        private String barColor;
    }

    @Data
    @Builder
    public static class SubjectCompletion {
        private String id;
        private String name;
        private int totalTopics;
        private int completedTopics;
        private int pendingTopics;
        private double completionPercentage;
        private String status;
        private String colorClass;
    }

    @Data
    @Builder
    public static class TeacherPerformance {
        private String name;
        private String avatar;
        private String bgClass;
        private int classesCount;
        private double completionPercentage;
        private String status;
        private String colorClass;
    }

    @Data
    @Builder
    public static class Deadline {
        private String subjectName;
        private String chapterName;
        private LocalDate date;
        private long daysLeft;
        private String leftColorClass;
    }
}
