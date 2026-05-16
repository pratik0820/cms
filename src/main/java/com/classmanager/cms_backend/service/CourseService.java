package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CourseSubjectGroupRequest;
import com.classmanager.cms_backend.dto.request.CreateCourseRequest;
import com.classmanager.cms_backend.dto.request.CreateSubjectRequest;
import com.classmanager.cms_backend.dto.request.UpdateCourseRequest;
import com.classmanager.cms_backend.dto.request.UpdateSubjectRequest;
import com.classmanager.cms_backend.dto.response.CourseResponse;
import com.classmanager.cms_backend.dto.response.SubjectGroupResponse;
import com.classmanager.cms_backend.dto.response.SubjectResponse;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.entity.SubjectGroup;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.CourseRepository;
import com.classmanager.cms_backend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    @Cacheable("subjects")
    @Transactional(readOnly = true)
    public List<SubjectResponse> listAllSubjects() {
        return subjectRepository.findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc()
                .stream()
                .map(this::toSubjectResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "subjects", allEntries = true)
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        String normalizedCode = normalizeCode(request.getCode(), "Subject code is required");
        if (subjectRepository.existsByCodeIgnoreCaseAndIsDeletedFalse(normalizedCode)) {
            throw new ResourceAlreadyExistsException("A subject with this code already exists.");
        }

        Subject subject = Subject.builder()
                .code(normalizedCode)
                .displayName(required(request.getDisplayName(), "Display name is required"))
                .shortName(trimToNull(request.getShortName()))
                .description(trimToNull(request.getDescription()))
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();
        return toSubjectResponse(subjectRepository.save(subject));
    }

    @Transactional
    @CacheEvict(value = "subjects", allEntries = true)
    public SubjectResponse updateSubject(UUID subjectId, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findByIdAndIsDeletedFalse(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));

        if (request.getCode() != null) {
            String normalizedCode = normalizeCode(request.getCode(), "Subject code is required");
            subjectRepository.findByCodeIgnoreCaseAndIsDeletedFalse(normalizedCode)
                    .filter(existing -> !existing.getId().equals(subjectId))
                    .ifPresent(existing -> {
                        throw new ResourceAlreadyExistsException("A subject with this code already exists.");
                    });
            subject.setCode(normalizedCode);
        }
        if (request.getDisplayName() != null) {
            subject.setDisplayName(required(request.getDisplayName(), "Display name is required"));
        }
        if (request.getShortName() != null) subject.setShortName(trimToNull(request.getShortName()));
        if (request.getDescription() != null) subject.setDescription(trimToNull(request.getDescription()));
        if (request.getSortOrder() != null) subject.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) subject.setIsActive(request.getIsActive());

        return toSubjectResponse(subjectRepository.save(subject));
    }

    @Transactional
    @CacheEvict(value = "subjects", allEntries = true)
    public void deleteSubject(UUID subjectId) {
        Subject subject = subjectRepository.findByIdAndIsDeletedFalse(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        subject.softDelete();
        subject.setIsActive(false);
        subjectRepository.save(subject);
    }

    @Cacheable("courses")
    @Transactional(readOnly = true)
    public List<CourseResponse> listAllCourses() {
        return courseRepository.findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc()
                .stream()
                .map(course -> toCourseResponse(course, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> listCourses(BoardType board, CourseCategory category, String standard) {
        String normalizedStandard = trimToNull(standard);
        List<Course> courses;
        if (board != null && normalizedStandard != null) {
            courses = courseRepository.findByBoardAndStandardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(
                    board, normalizedStandard);
        } else if (board != null) {
            courses = courseRepository.findByBoardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(board);
        } else if (category != null && normalizedStandard != null) {
            courses = courseRepository.findByCategoryAndStandardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(
                    category, normalizedStandard);
        } else if (category != null) {
            courses = courseRepository.findByCategoryAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(category);
        } else if (normalizedStandard != null) {
            courses = courseRepository.findByStandardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(normalizedStandard);
        } else {
            courses = courseRepository.findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc();
        }

        return courses.stream()
                .map(course -> toCourseResponse(course, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseDetail(UUID courseId) {
        Course course = courseRepository.findByIdWithSubjectGroups(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return toCourseResponse(course, true);
    }

    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse createCourse(CreateCourseRequest request) {
        String normalizedCode = normalizeCode(request.getCode(), "Course code is required");
        if (courseRepository.existsByCodeAndIsDeletedFalse(normalizedCode)) {
            throw new ResourceAlreadyExistsException("A course with this code already exists.");
        }

        Course course = Course.builder()
                .name(required(request.getName(), "Course name is required"))
                .code(normalizedCode)
                .category(request.getCategory())
                .board(request.getBoard())
                .standard(required(request.getStandard(), "Standard is required"))
                .academicYear(trimToNull(request.getAcademicYear()))
                .description(trimToNull(request.getDescription()))
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() == null || request.getIsActive())
                .subjectGroups(new ArrayList<>())
                .build();

        applySubjectGroups(course, request.getSubjectGroups());
        course = courseRepository.save(course);
        return toCourseResponse(course, true);
    }

    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse updateCourse(UUID courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findByIdWithSubjectGroups(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (request.getCode() != null) {
            String normalizedCode = normalizeCode(request.getCode(), "Course code is required");
            courseRepository.findByCodeAndIsDeletedFalse(normalizedCode)
                    .filter(existing -> !existing.getId().equals(courseId))
                    .ifPresent(existing -> {
                        throw new ResourceAlreadyExistsException("A course with this code already exists.");
                    });
            course.setCode(normalizedCode);
        }
        if (request.getName() != null) course.setName(required(request.getName(), "Course name is required"));
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getBoard() != null) course.setBoard(request.getBoard());
        if (request.getStandard() != null) course.setStandard(required(request.getStandard(), "Standard is required"));
        if (request.getAcademicYear() != null) course.setAcademicYear(trimToNull(request.getAcademicYear()));
        if (request.getDescription() != null) course.setDescription(trimToNull(request.getDescription()));
        if (request.getSortOrder() != null) course.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) course.setIsActive(request.getIsActive());
        if (request.getSubjectGroups() != null) {
            applySubjectGroups(course, request.getSubjectGroups());
        }

        course = courseRepository.save(course);
        return toCourseResponse(course, true);
    }

    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public void deleteCourse(UUID courseId) {
        Course course = courseRepository.findByIdAndIsDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        course.softDelete();
        course.setIsActive(false);
        courseRepository.save(course);
    }

    public SubjectResponse toSubjectResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .displayName(subject.getDisplayName())
                .shortName(subject.getShortName())
                .description(subject.getDescription())
                .sortOrder(subject.getSortOrder())
                .isActive(subject.getIsActive())
                .build();
    }

    public CourseResponse toCourseResponse(Course course, boolean includeGroups) {
        CourseResponse.CourseResponseBuilder builder = CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .category(course.getCategory())
                .board(course.getBoard())
                .standard(course.getStandard())
                .academicYear(course.getAcademicYear())
                .description(course.getDescription())
                .isActive(course.getIsActive())
                .sortOrder(course.getSortOrder());

        if (includeGroups && course.getSubjectGroups() != null) {
            builder.subjectGroups(course.getSubjectGroups().stream()
                    .filter(group -> !group.isDeleted() && Boolean.TRUE.equals(group.getIsActive()))
                    .map(this::toSubjectGroupResponse)
                    .toList());
        }
        return builder.build();
    }

    public SubjectGroupResponse toSubjectGroupResponse(SubjectGroup group) {
        return SubjectGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .shortName(group.getShortName())
                .subjectCount(group.getSubjectCount())
                .isExtraSubjectAllowed(group.getIsExtraSubjectAllowed())
                .maxExtraSubjects(group.getMaxExtraSubjects())
                .subjects(group.getSubjects().stream()
                        .filter(subject -> !subject.isDeleted() && Boolean.TRUE.equals(subject.getIsActive()))
                        .map(this::toSubjectResponse)
                        .toList())
                .allowedExtraSubjects(group.getAllowedExtraSubjects().stream()
                        .filter(subject -> !subject.isDeleted() && Boolean.TRUE.equals(subject.getIsActive()))
                        .map(this::toSubjectResponse)
                        .toList())
                .sortOrder(group.getSortOrder())
                .isActive(group.getIsActive())
                .build();
    }

    private void applySubjectGroups(Course course, List<CourseSubjectGroupRequest> requests) {
        course.getSubjectGroups().clear();
        if (requests == null || requests.isEmpty()) {
            return;
        }

        int index = 0;
        for (CourseSubjectGroupRequest request : requests) {
            List<Subject> subjects = resolveActiveSubjects(request.getSubjectIds());
            List<Subject> allowedExtraSubjects = resolveActiveSubjects(request.getAllowedExtraSubjectIds());
            boolean extraAllowed = Boolean.TRUE.equals(request.getIsExtraSubjectAllowed());

            if (!extraAllowed && !allowedExtraSubjects.isEmpty()) {
                throw new BadRequestException(
                        "Allowed extra subjects can only be set when extra subjects are enabled for the group",
                        "INVALID_SUBJECT_GROUP");
            }

            SubjectGroup group = SubjectGroup.builder()
                    .course(course)
                    .name(required(request.getName(), "Subject group name is required"))
                    .shortName(trimToNull(request.getShortName()))
                    .subjectCount(subjects.size())
                    .isExtraSubjectAllowed(extraAllowed)
                    .maxExtraSubjects(request.getMaxExtraSubjects() != null ? request.getMaxExtraSubjects() : 0)
                    .subjects(new ArrayList<>(subjects))
                    .allowedExtraSubjects(extraAllowed ? new ArrayList<>(allowedExtraSubjects) : new ArrayList<>())
                    .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : index)
                    .isActive(request.getIsActive() == null || request.getIsActive())
                    .build();
            course.getSubjectGroups().add(group);
            index++;
        }
    }

    private List<Subject> resolveActiveSubjects(List<UUID> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return List.of();
        }
        return subjectIds.stream()
                .distinct()
                .map(subjectId -> subjectRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(subjectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId)))
                .toList();
    }

    private String normalizeCode(String value, String message) {
        return required(value, message)
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ENGLISH);
    }

    private String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message, "VALIDATION_ERROR");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
