package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.CourseResponse;
import com.classmanager.cms_backend.dto.response.SubjectGroupResponse;
import com.classmanager.cms_backend.dto.response.SubjectResponse;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.entity.SubjectGroup;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.CourseRepository;
import com.classmanager.cms_backend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    // ── Subjects ─────────────────────────────────────────────────────────────

    @Cacheable("subjects")
    @Transactional(readOnly = true)
    public List<SubjectResponse> listAllSubjects() {
        return subjectRepository.findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc()
                .stream()
                .map(this::toSubjectResponse)
                .toList();
    }

    // ── Courses ───────────────────────────────────────────────────────────────

    @Cacheable("courses")
    @Transactional(readOnly = true)
    public List<CourseResponse> listAllCourses() {
        return courseRepository.findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc()
                .stream()
                .map(c -> toCourseResponse(c, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> listCoursesByBoard(BoardType board) {
        return courseRepository.findByBoardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(board)
                .stream()
                .map(c -> toCourseResponse(c, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> listCoursesByCategory(CourseCategory category) {
        return courseRepository.findByCategoryAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(category)
                .stream()
                .map(c -> toCourseResponse(c, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseDetail(UUID courseId) {
        Course course = courseRepository.findByIdWithSubjectGroups(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return toCourseResponse(course, true);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    public SubjectResponse toSubjectResponse(Subject s) {
        return SubjectResponse.builder()
                .id(s.getId())
                .code(s.getCode())
                .displayName(s.getDisplayName())
                .shortName(s.getShortName())
                .description(s.getDescription())
                .sortOrder(s.getSortOrder())
                .isActive(s.getIsActive())
                .build();
    }

    public CourseResponse toCourseResponse(Course c, boolean includeGroups) {
        CourseResponse.CourseResponseBuilder builder = CourseResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .code(c.getCode())
                .category(c.getCategory())
                .board(c.getBoard())
                .standard(c.getStandard())
                .academicYear(c.getAcademicYear())
                .description(c.getDescription())
                .isActive(c.getIsActive())
                .sortOrder(c.getSortOrder());

        if (includeGroups && c.getSubjectGroups() != null) {
            builder.subjectGroups(
                    c.getSubjectGroups().stream()
                            .filter(sg -> !sg.isDeleted() && Boolean.TRUE.equals(sg.getIsActive()))
                            .map(this::toSubjectGroupResponse)
                            .toList()
            );
        }
        return builder.build();
    }

    public SubjectGroupResponse toSubjectGroupResponse(SubjectGroup sg) {
        return SubjectGroupResponse.builder()
                .id(sg.getId())
                .name(sg.getName())
                .shortName(sg.getShortName())
                .subjectCount(sg.getSubjectCount())
                .isExtraSubjectAllowed(sg.getIsExtraSubjectAllowed())
                .maxExtraSubjects(sg.getMaxExtraSubjects())
                .subjects(sg.getSubjects().stream().map(this::toSubjectResponse).toList())
                .allowedExtraSubjects(sg.getAllowedExtraSubjects().stream().map(this::toSubjectResponse).toList())
                .sortOrder(sg.getSortOrder())
                .isActive(sg.getIsActive())
                .build();
    }
}
