package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.SyllabusDashboardResponse;
import com.classmanager.cms_backend.entity.BatchSubtopicProgress;
import com.classmanager.cms_backend.repository.BatchSubtopicProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Subtopic;
import com.classmanager.cms_backend.repository.BatchRepository;
import com.classmanager.cms_backend.repository.SubtopicRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Service
@RequiredArgsConstructor
public class SyllabusDashboardService {

    private final BatchSubtopicProgressRepository progressRepository;
    private final BatchRepository batchRepository;
    private final SubtopicRepository subtopicRepository;

    @Getter
    @AllArgsConstructor
    public static class VirtualRecord {
        private Batch batch;
        private Subtopic subtopic;
        private BatchSubtopicProgress progress;

        public String getStatus() {
            return progress != null ? progress.getStatus() : "PENDING";
        }

        public Integer getCompletedQuestions() {
            return progress != null && progress.getCompletedQuestions() != null ? progress.getCompletedQuestions() : 0;
        }
    }

    @Transactional(readOnly = true)
    public SyllabusDashboardResponse getDashboardStats(UUID branchId, String standard, UUID subjectId, UUID teacherId, String status, LocalDate startDate, LocalDate endDate) {
        
        List<Batch> batches = batchRepository.findDashboardBatches(branchId, standard);
        if (batches.isEmpty()) {
            return emptyResponse();
        }

        List<UUID> batchIds = batches.stream().map(Batch::getId).toList();
        List<UUID> courseIds = batches.stream().map(b -> b.getCourse().getId()).distinct().toList();

        List<Subtopic> subtopics = subtopicRepository.findSubtopicsForCourses(courseIds);
        List<BatchSubtopicProgress> progresses = progressRepository.findProgressForBatches(batchIds);

        // batchId -> subtopicId -> progress
        Map<UUID, Map<UUID, BatchSubtopicProgress>> progressMap = new HashMap<>();
        for (BatchSubtopicProgress p : progresses) {
            progressMap.computeIfAbsent(p.getBatch().getId(), k -> new HashMap<>()).put(p.getSubtopic().getId(), p);
        }

        List<VirtualRecord> records = new ArrayList<>();
        for (Batch batch : batches) {
            for (Subtopic subtopic : subtopics) {
                if (!subtopic.getChapter().getCourse().getId().equals(batch.getCourse().getId())) {
                    continue;
                }
                
                BatchSubtopicProgress p = progressMap.getOrDefault(batch.getId(), Collections.emptyMap()).get(subtopic.getId());
                records.add(new VirtualRecord(batch, subtopic, p));
            }
        }

        // Apply remaining filters in-memory
        records = records.stream().filter(r -> {
            if (subjectId != null && !r.getSubtopic().getChapter().getSubject().getId().equals(subjectId)) {
                return false;
            }
            if (status != null && !status.equalsIgnoreCase("All Status") && !r.getStatus().equalsIgnoreCase(status)) {
                return false;
            }
            if (teacherId != null) {
                // If filtering by teacher, we only include topics updated by this teacher.
                if (r.getProgress() == null || r.getProgress().getCreatedByUser() == null || !r.getProgress().getCreatedByUser().getId().equals(teacherId)) {
                    return false;
                }
            }
            if (startDate != null || endDate != null) {
                if (r.getProgress() == null || r.getProgress().getUpdatedAt() == null) {
                    return false;
                }
                LocalDate updatedDate = r.getProgress().getUpdatedAt().toLocalDate();
                if (startDate != null && updatedDate.isBefore(startDate)) {
                    return false;
                }
                if (endDate != null && updatedDate.isAfter(endDate)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        // Aggregation logic
        int totalSubjects = (int) records.stream().map(r -> r.getSubtopic().getChapter().getSubject().getId()).distinct().count();
        int totalClasses = (int) records.stream().map(r -> r.getBatch().getId()).distinct().count();
        int totalTeachers = (int) records.stream().filter(r -> r.getProgress() != null && r.getProgress().getCreatedByUser() != null)
                .map(r -> r.getProgress().getCreatedByUser().getId()).distinct().count();

        long totalQuestions = records.stream().mapToLong(r -> r.getSubtopic().getNoOfQuestions() != null ? r.getSubtopic().getNoOfQuestions() : 0).sum();
        long totalCompleted = records.stream().mapToLong(VirtualRecord::getCompletedQuestions).sum();
        
        double overallCompletionPercentage = totalQuestions > 0 ? (double) totalCompleted / totalQuestions * 100.0 : 0.0;
        int pendingTopicsCount = (int) records.stream().filter(r -> !"COMPLETED".equals(r.getStatus())).count();

        SyllabusDashboardResponse.OverviewStats overviewStats = SyllabusDashboardResponse.OverviewStats.builder()
                .totalSubjects(totalSubjects)
                .totalClasses(totalClasses)
                .totalTeachers(totalTeachers)
                .overallCompletionPercentage(Math.round(overallCompletionPercentage * 10.0) / 10.0)
                .pendingTopics(pendingTopicsCount)
                .build();

        // Monthly Trend
        Map<YearMonth, Double> monthlyTotals = new TreeMap<>();
        Map<YearMonth, Double> monthlyCompleted = new TreeMap<>();
        for (VirtualRecord r : records) {
            YearMonth month;
            if (r.getProgress() != null && r.getProgress().getUpdatedAt() != null) {
                month = YearMonth.from(r.getProgress().getUpdatedAt());
            } else if (r.getProgress() != null && r.getProgress().getCreatedAt() != null) {
                month = YearMonth.from(r.getProgress().getCreatedAt());
            } else {
                month = YearMonth.from(r.getSubtopic().getCreatedAt()); // Fallback for pending topics
            }
            monthlyTotals.put(month, monthlyTotals.getOrDefault(month, 0.0) + (r.getSubtopic().getNoOfQuestions() != null ? r.getSubtopic().getNoOfQuestions() : 0));
            monthlyCompleted.put(month, monthlyCompleted.getOrDefault(month, 0.0) + r.getCompletedQuestions());
        }

        List<SyllabusDashboardResponse.MonthlyTrend> trends = monthlyTotals.entrySet().stream().map(e -> {
            YearMonth m = e.getKey();
            double total = e.getValue();
            double comp = monthlyCompleted.getOrDefault(m, 0.0);
            double pct = total > 0 ? (comp / total * 100) : 0;
            return SyllabusDashboardResponse.MonthlyTrend.builder()
                    .month(m.format(DateTimeFormatter.ofPattern("MMM")))
                    .value(Math.round(pct * 10.0) / 10.0)
                    .build();
        }).collect(Collectors.toList());

        // Status Distribution
        long completedCount = records.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count();
        long inProgressCount = records.stream().filter(r -> "IN_PROGRESS".equals(r.getStatus())).count();
        long notStartedCount = records.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        int totalStatusCount = records.size();

        SyllabusDashboardResponse.StatusDistribution statusDistribution = SyllabusDashboardResponse.StatusDistribution.builder()
                .completedCount((int) completedCount)
                .completedPercentage(totalStatusCount > 0 ? (double) completedCount / totalStatusCount * 100 : 0)
                .inProgressCount((int) inProgressCount)
                .inProgressPercentage(totalStatusCount > 0 ? (double) inProgressCount / totalStatusCount * 100 : 0)
                .notStartedCount((int) notStartedCount)
                .notStartedPercentage(totalStatusCount > 0 ? (double) notStartedCount / totalStatusCount * 100 : 0)
                .build();

        // Class Completions
        Map<String, List<VirtualRecord>> byClass = records.stream().collect(Collectors.groupingBy(r -> r.getBatch().getName() + " - " + r.getBatch().getCourse().getStandard()));
        List<SyllabusDashboardResponse.ClassCompletion> classCompletions = byClass.entrySet().stream().map(e -> {
            long tQ = e.getValue().stream().mapToLong(r -> r.getSubtopic().getNoOfQuestions() != null ? r.getSubtopic().getNoOfQuestions() : 0).sum();
            long cQ = e.getValue().stream().mapToLong(VirtualRecord::getCompletedQuestions).sum();
            double pct = tQ > 0 ? (double) cQ / tQ * 100 : 0;
            String statusClass = pct >= 90 ? "Excellent" : pct >= 75 ? "Very Good" : pct >= 60 ? "Good" : pct >= 40 ? "Average" : "Needs Improvement";
            String barColor = pct >= 90 ? "#10b981" : pct >= 75 ? "#34d399" : pct >= 60 ? "#3b82f6" : pct >= 40 ? "#f59e0b" : "#ef4444";
            return SyllabusDashboardResponse.ClassCompletion.builder()
                    .className(e.getKey())
                    .completionPercentage(Math.round(pct * 10.0) / 10.0)
                    .status(statusClass)
                    .barColor(barColor)
                    .build();
        }).sorted((a, b) -> Double.compare(b.getCompletionPercentage(), a.getCompletionPercentage())).collect(Collectors.toList());

        // Subject Completions
        Map<UUID, List<VirtualRecord>> bySubject = records.stream().collect(Collectors.groupingBy(r -> r.getSubtopic().getChapter().getSubject().getId()));
        List<SyllabusDashboardResponse.SubjectCompletion> subjectCompletions = bySubject.entrySet().stream().map(e -> {
            List<VirtualRecord> subRecords = e.getValue();
            String name = subRecords.get(0).getSubtopic().getChapter().getSubject().getDisplayName();
            int totalT = subRecords.size();
            int compT = (int) subRecords.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count();
            int pendT = totalT - compT;
            long tQ = subRecords.stream().mapToLong(r -> r.getSubtopic().getNoOfQuestions() != null ? r.getSubtopic().getNoOfQuestions() : 0).sum();
            long cQ = subRecords.stream().mapToLong(VirtualRecord::getCompletedQuestions).sum();
            double pct = tQ > 0 ? (double) cQ / tQ * 100 : 0;
            String statusClass = pct >= 90 ? "Excellent" : pct >= 75 ? "Very Good" : pct >= 60 ? "Good" : pct >= 40 ? "Average" : "Needs Improvement";
            String colClass = pct >= 90 ? "text-green-600" : pct >= 75 ? "text-green-500" : pct >= 60 ? "text-blue-600" : pct >= 40 ? "text-yellow-500" : "text-orange-500";
            return SyllabusDashboardResponse.SubjectCompletion.builder()
                    .id(e.getKey().toString())
                    .name(name)
                    .totalTopics(totalT)
                    .completedTopics(compT)
                    .pendingTopics(pendT)
                    .completionPercentage(Math.round(pct * 10.0) / 10.0)
                    .status(statusClass)
                    .colorClass(colClass)
                    .build();
        }).sorted((a, b) -> Double.compare(b.getCompletionPercentage(), a.getCompletionPercentage())).collect(Collectors.toList());

        // Teacher Performances (simplified using createdBy as proxy)
        Map<UUID, List<VirtualRecord>> byTeacher = records.stream().filter(r -> r.getProgress() != null && r.getProgress().getCreatedByUser() != null)
                .collect(Collectors.groupingBy(r -> r.getProgress().getCreatedByUser().getId()));
        List<SyllabusDashboardResponse.TeacherPerformance> teacherPerformances = byTeacher.entrySet().stream().map(e -> {
            List<VirtualRecord> tRecords = e.getValue();
            String name = tRecords.get(0).getProgress().getCreatedByUser().getFullName();
            String avatar = "U";
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.trim().split("\\s+");
                if (parts.length > 1) {
                    avatar = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
                } else {
                    avatar = name.substring(0, Math.min(2, name.length())).toUpperCase();
                }
            }
            int classes = (int) tRecords.stream().map(r -> r.getBatch().getId()).distinct().count();
            long tQ = tRecords.stream().mapToLong(r -> r.getSubtopic().getNoOfQuestions() != null ? r.getSubtopic().getNoOfQuestions() : 0).sum();
            long cQ = tRecords.stream().mapToLong(VirtualRecord::getCompletedQuestions).sum();
            double pct = tQ > 0 ? (double) cQ / tQ * 100 : 0;
            String statusClass = pct >= 90 ? "Excellent" : pct >= 75 ? "Very Good" : pct >= 60 ? "Good" : pct >= 40 ? "Average" : "Needs Improvement";
            String colClass = pct >= 90 ? "text-green-600" : pct >= 75 ? "text-green-500" : pct >= 60 ? "text-blue-600" : pct >= 40 ? "text-yellow-500" : "text-orange-500";
            return SyllabusDashboardResponse.TeacherPerformance.builder()
                    .name(name)
                    .avatar(avatar)
                    .bgClass("bg-blue-100")
                    .classesCount(classes)
                    .completionPercentage(Math.round(pct * 10.0) / 10.0)
                    .status(statusClass)
                    .colorClass(colClass)
                    .build();
        }).sorted((a, b) -> Double.compare(b.getCompletionPercentage(), a.getCompletionPercentage())).collect(Collectors.toList());

        // Upcoming Deadlines (Find chapters with target_date >= today, ordered by date)
        LocalDate today = LocalDate.now();
        List<SyllabusDashboardResponse.Deadline> upcomingDeadlines = records.stream()
                .filter(r -> r.getSubtopic().getChapter().getTargetDate() != null)
                .filter(r -> !r.getSubtopic().getChapter().getTargetDate().isBefore(today))
                // deduplicate by chapter
                .collect(Collectors.toMap(r -> r.getSubtopic().getChapter().getId(), r -> r, (r1, r2) -> r1))
                .values().stream()
                .map(r -> {
                    LocalDate target = r.getSubtopic().getChapter().getTargetDate();
                    long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, target);
                    String leftCol = daysLeft <= 7 ? "bg-red-100 text-red-600" : daysLeft <= 15 ? "bg-orange-100 text-orange-600" : "bg-yellow-100 text-yellow-600";
                    return SyllabusDashboardResponse.Deadline.builder()
                            .subjectName(r.getSubtopic().getChapter().getSubject().getDisplayName() + " - " + r.getBatch().getCourse().getStandard())
                            .chapterName("Chapter: " + r.getSubtopic().getChapter().getName())
                            .date(target)
                            .daysLeft(daysLeft)
                            .leftColorClass(leftCol)
                            .build();
                })
                .sorted(Comparator.comparing(SyllabusDashboardResponse.Deadline::getDate))
                .limit(8)
                .collect(Collectors.toList());

        return SyllabusDashboardResponse.builder()
                .stats(overviewStats)
                .trends(trends)
                .statusDistribution(statusDistribution)
                .classCompletions(classCompletions)
                .subjectCompletions(subjectCompletions)
                .teacherPerformances(teacherPerformances)
                .upcomingDeadlines(upcomingDeadlines)
                .build();
    }

    private SyllabusDashboardResponse emptyResponse() {
        return SyllabusDashboardResponse.builder()
                .stats(SyllabusDashboardResponse.OverviewStats.builder().build())
                .trends(List.of())
                .statusDistribution(SyllabusDashboardResponse.StatusDistribution.builder().build())
                .classCompletions(List.of())
                .subjectCompletions(List.of())
                .teacherPerformances(List.of())
                .upcomingDeadlines(List.of())
                .build();
    }
}
