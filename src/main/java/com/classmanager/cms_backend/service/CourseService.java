package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.request.UpdateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.response.AcademicCourseDetailResponse;
import com.classmanager.cms_backend.dto.response.AcademicCourseListItemResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Standard;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.CourseManagementRepository;
import com.classmanager.cms_backend.repository.StandardManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final CourseManagementRepository courseManagementRepository;
    private final StandardManagementRepository standardManagementRepository;
    private final AcademicCourseResponseMapper academicCourseResponseMapper;

    @Transactional(readOnly = true)
    public PagedResponse<AcademicCourseListItemResponse> listCourses(UUID branchId, UUID currentBranchId, String search, int page, int size) {
        UUID resolvedBranchId = branchId != null ? branchId : currentBranchId;
        Page<AcademicCourseListItemResponse> result = courseManagementRepository.searchCourseRows(
                        resolvedBranchId,
                        buildSearchPattern(search),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayCode")))
                .map(academicCourseResponseMapper::toListItem);
        return PagedResponse.from(result);
    }

    @Transactional
    public AcademicCourseDetailResponse createCourse(UUID branchId, UUID currentBranchId, CreateAcademicCourseRequest request) {
        Standard standard = loadStandard(request.getStandard(), request.getBoard());
        Branch branch = loadBranchForCreate(branchId, currentBranchId);
        validateTimeRange(request.getStartTime(), request.getEndTime());

        Course course = Course.builder()
                .name(normalizeRequired(request.getCourseName(), "Course name is required"))
                .code(UUID.randomUUID().toString())
                .standardRef(standard)
                .category(resolveCategory(request.getBoard()))
                .board(request.getBoard())
                .standard(standard.getName())
                .academicYear(normalizeRequired(request.getAcademicYear(), "Academic year is required"))
                .medium(normalizeRequired(request.getMedium(), "Medium is required"))
                .isActive(true)
                .build();
        course = courseManagementRepository.saveCourse(course);

        Batch batch = Batch.builder()
                .branch(branch)
                .course(course)
                .displayCode(nextBatchCode())
                .name(normalizeRequired(request.getBatchName(), "Batch name is required"))
                .academicYear(course.getAcademicYear())
                .timing(request.getBatchTiming())
                .timingLabel(buildTimingLabel(request.getStartTime(), request.getEndTime()))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(true)
                .build();
        batch = courseManagementRepository.saveBatch(batch);

        Course savedCourse = loadCourse(course.getId());
        return academicCourseResponseMapper.toDetail(savedCourse, batch);
    }

    @Transactional(readOnly = true)
    public AcademicCourseDetailResponse getCourse(UUID courseId, UUID branchId, UUID currentBranchId) {
        Course course = loadCourse(courseId);
        Batch batch = loadBatchForCourse(course, branchId, currentBranchId);
        return academicCourseResponseMapper.toDetail(course, batch);
    }

    @Transactional
    public AcademicCourseDetailResponse updateCourse(UUID courseId, UUID branchId, UUID currentBranchId, UpdateAcademicCourseRequest request) {
        Course course = loadCourse(courseId);
        Batch batch = loadBatchForCourse(course, branchId, currentBranchId);
        Standard standard = loadStandard(request.getStandard(), request.getBoard());
        validateTimeRange(request.getStartTime(), request.getEndTime());

        course.setStandardRef(standard);
        course.setBoard(request.getBoard());
        course.setStandard(standard.getName());
        course.setMedium(normalizeRequired(request.getMedium(), "Medium is required"));
        course.setAcademicYear(normalizeRequired(request.getAcademicYear(), "Academic year is required"));
        course.setName(normalizeRequired(request.getCourseName(), "Course name is required"));

        batch.setName(normalizeRequired(request.getBatchName(), "Batch name is required"));
        batch.setAcademicYear(course.getAcademicYear());
        batch.setTiming(request.getBatchTiming());
        batch.setTimingLabel(buildTimingLabel(request.getStartTime(), request.getEndTime()));
        batch.setStartTime(request.getStartTime());
        batch.setEndTime(request.getEndTime());

        courseManagementRepository.saveCourse(course);
        courseManagementRepository.saveBatch(batch);

        Course updatedCourse = loadCourse(courseId);
        Batch updatedBatch = loadBatchForCourse(updatedCourse, branchId, currentBranchId);
        return academicCourseResponseMapper.toDetail(updatedCourse, updatedBatch);
    }

    @Transactional
    public void deleteCourse(UUID courseId, UUID branchId, UUID currentBranchId) {
        Course course = loadCourse(courseId);
        Batch batch = loadBatchForCourse(course, branchId, currentBranchId);

        batch.softDelete();
        batch.setIsActive(false);
        courseManagementRepository.saveBatch(batch);

        course.softDelete();
        course.setIsActive(false);
        courseManagementRepository.saveCourse(course);
    }

    private Standard loadStandard(String standardName, BoardType board) {
        return standardManagementRepository.findByNameAndBoard(normalizeRequired(standardName, "Class / standard is required"), board)
                .orElseThrow(() -> new BadRequestException("Selected standard and board do not exist.", "STANDARD_NOT_FOUND"));
    }

    private Branch loadBranchForCreate(UUID branchId, UUID currentBranchId) {
        UUID resolvedBranchId = branchId != null ? branchId : currentBranchId;
        if (resolvedBranchId != null) {
            return courseManagementRepository.findBranch(resolvedBranchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", resolvedBranchId));
        }
        return courseManagementRepository.findFirstActiveBranch()
                .orElseThrow(() -> new BadRequestException("No active branch available for course creation.", "BRANCH_REQUIRED"));
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

    private CourseCategory resolveCategory(BoardType board) {
        return board == BoardType.HSC ? CourseCategory.BOARD_SENIOR : CourseCategory.BOARD_REGULAR;
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("End time must be after start time.", "INVALID_TIME_RANGE");
        }
    }

    private String buildTimingLabel(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        return startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER);
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message, "VALIDATION_ERROR");
        }
        return value.trim();
    }

    private String buildSearchPattern(String value) {
        return StringUtils.hasText(value) ? "%" + value.trim().toLowerCase(Locale.ENGLISH) + "%" : null;
    }

    private String nextBatchCode() {
        long index = Math.max(courseManagementRepository.countBatches() + 1, 1);
        String candidate;
        do {
            candidate = "CRS" + String.format("%04d", index);
            index++;
        } while (courseManagementRepository.existsBatchDisplayCode(candidate));
        return candidate;
    }
}
