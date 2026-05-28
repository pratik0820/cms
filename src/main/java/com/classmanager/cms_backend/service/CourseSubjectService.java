package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.request.UpdateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.response.CourseSubjectResponse;
import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.CourseManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseSubjectService {

    private final CourseManagementRepository courseManagementRepository;
    private final AcademicSubjectResponseMapper academicSubjectResponseMapper;

    @Transactional
    public CourseSubjectResponse addSubject(UUID courseId, UUID branchId, UUID currentBranchId, CreateCourseSubjectRequest request) {
        Course course = loadCourse(courseId);
        loadBatchForCourse(course, branchId, currentBranchId);

        String subjectName = normalizeRequired(request.getSubjectName(), "Subject name is required");
        boolean alreadyAttached = course.getSubjects().stream()
                .anyMatch(subject -> subjectName.equalsIgnoreCase(subject.getDisplayName()));
        if (alreadyAttached) {
            throw new ResourceAlreadyExistsException("This subject is already added to the course.");
        }

        Subject subject = Subject.builder()
                .displayCode(nextSubjectCode())
                .code(nextSubjectValue(subjectName))
                .displayName(subjectName)
                .shortName(subjectName)
                .isActive(true)
                .build();
        if (courseManagementRepository.existsSubjectCode(subject.getCode())) {
            subject.setCode(subject.getCode() + "_" + subject.getDisplayCode());
        }

        Subject savedSubject = courseManagementRepository.saveSubject(subject);
        course.getSubjects().add(savedSubject);
        courseManagementRepository.saveCourse(course);
        return academicSubjectResponseMapper.toResponse(savedSubject);
    }

    @Transactional
    public CourseSubjectResponse updateSubject(UUID courseId, UUID subjectId, UUID branchId, UUID currentBranchId, UpdateCourseSubjectRequest request) {
        Course course = loadCourse(courseId);
        loadBatchForCourse(course, branchId, currentBranchId);

        Subject subject = course.getSubjects().stream()
                .filter(item -> item.getId().equals(subjectId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));

        subject.setDisplayName(normalizeRequired(request.getSubjectName(), "Subject name is required"));
        subject.setShortName(subject.getDisplayName());
        Subject savedSubject = courseManagementRepository.saveSubject(subject);
        return academicSubjectResponseMapper.toResponse(savedSubject);
    }

    @Transactional
    public void deleteSubject(UUID courseId, UUID subjectId, UUID branchId, UUID currentBranchId) {
        Course course = loadCourse(courseId);
        loadBatchForCourse(course, branchId, currentBranchId);

        Subject subject = course.getSubjects().stream()
                .filter(item -> item.getId().equals(subjectId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));

        course.getSubjects().remove(subject);
        courseManagementRepository.saveCourse(course);

        subject.softDelete();
        subject.setIsActive(false);
        courseManagementRepository.saveSubject(subject);
    }

    private Course loadCourse(UUID courseId) {
        return courseManagementRepository.findCourseDetail(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }

    private Batch loadBatchForCourse(Course course, UUID branchId, UUID currentBranchId) {
        UUID resolvedBranchId = branchId != null ? branchId : currentBranchId;
        if (resolvedBranchId != null) {
            return courseManagementRepository.findActiveBatchByBranchAndCourse(resolvedBranchId, course.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", course.getId()));
        }

        List<Batch> activeBatches = courseManagementRepository.findActiveBatchesByCourse(course.getId());
        if (activeBatches.isEmpty()) {
            throw new ResourceNotFoundException("Course", course.getId());
        }
        if (activeBatches.size() > 1) {
            throw new BadRequestException("Branch is required when a course is linked to multiple active batches.", "BRANCH_REQUIRED");
        }
        return activeBatches.get(0);
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message, "VALIDATION_ERROR");
        }
        return value.trim();
    }

    private String nextSubjectCode() {
        long index = Math.max(courseManagementRepository.countSubjects() + 1, 1);
        String candidate;
        do {
            candidate = "SUB" + String.format("%04d", index);
            index++;
        } while (courseManagementRepository.existsSubjectDisplayCode(candidate));
        return candidate;
    }

    private String nextSubjectValue(String subjectName) {
        String normalized = subjectName.toUpperCase(Locale.ENGLISH).replaceAll("[^A-Z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        if (!StringUtils.hasText(normalized)) {
            normalized = "SUBJECT";
        }
        return normalized.length() > 40 ? normalized.substring(0, 40) : normalized;
    }
}
