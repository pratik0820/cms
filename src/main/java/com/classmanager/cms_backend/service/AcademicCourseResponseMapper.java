package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.AcademicCourseDetailResponse;
import com.classmanager.cms_backend.dto.response.AcademicCourseListItemResponse;
import com.classmanager.cms_backend.dto.response.CourseSubjectResponse;
import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AcademicCourseResponseMapper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final AcademicSubjectResponseMapper subjectResponseMapper;

    public AcademicCourseListItemResponse toListItem(Batch batch) {
        Course course = batch.getCourse();
        return AcademicCourseListItemResponse.builder()
                .id(course.getId())
                .courseId(course.getId())
                .batchId(batch.getId())
                .batchCode(resolveBatchCode(batch))
                .standard(course.getStandard())
                .board(course.getBoard() != null ? course.getBoard().getDisplayName() : null)
                .medium(course.getMedium())
                .academicYear(course.getAcademicYear())
                .courseName(course.getName())
                .batchName(batch.getName())
                .batchTiming(buildTimingLabel(batch.getStartTime(), batch.getEndTime()))
                .build();
    }

    public AcademicCourseDetailResponse toDetail(Course course, Batch batch) {
        List<CourseSubjectResponse> subjects = course.getSubjects() == null ? List.of() : course.getSubjects().stream()
                .filter(subject -> !subject.isDeleted())
                .sorted(Comparator.comparing(Subject::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .map(subjectResponseMapper::toResponse)
                .toList();

        return AcademicCourseDetailResponse.builder()
                .id(course.getId())
                .courseId(course.getId())
                .batchId(batch != null ? batch.getId() : null)
                .batchCode(batch != null ? resolveBatchCode(batch) : null)
                .standard(course.getStandard())
                .board(course.getBoard() != null ? course.getBoard().getDisplayName() : null)
                .medium(course.getMedium())
                .academicYear(course.getAcademicYear())
                .courseName(course.getName())
                .batchName(batch != null ? batch.getName() : null)
                .batchTiming(batch != null ? buildTimingLabel(batch.getStartTime(), batch.getEndTime()) : null)
                .startTime(batch != null ? batch.getStartTime() : null)
                .endTime(batch != null ? batch.getEndTime() : null)
                .subjects(subjects)
                .build();
    }

    private String buildTimingLabel(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        return startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER);
    }

    private String resolveBatchCode(Batch batch) {
        return StringUtils.hasText(batch.getDisplayCode()) ? batch.getDisplayCode() : "CRS-" + batch.getId();
    }
}
