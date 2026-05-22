package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.request.CreateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.request.CreateStandardRequest;
import com.classmanager.cms_backend.dto.request.UpdateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.request.UpdateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.request.UpdateStandardRequest;
import com.classmanager.cms_backend.dto.response.AcademicCourseDetailResponse;
import com.classmanager.cms_backend.dto.response.AcademicCourseListItemResponse;
import com.classmanager.cms_backend.dto.response.BoardOptionResponse;
import com.classmanager.cms_backend.dto.response.CourseSubjectResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.dto.response.StandardResponse;
import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Standard;
import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.enums.BatchTiming;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BatchRepository;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.CourseRepository;
import com.classmanager.cms_backend.repository.StandardRepository;
import com.classmanager.cms_backend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcademicManagementService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final StandardRepository standardRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final SubjectRepository subjectRepository;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public PagedResponse<StandardResponse> listStandards(String search, int page, int size) {
        Page<StandardResponse> result = standardRepository.search(buildSearchPattern(search), PageRequest.of(
                        page, size, Sort.by(Sort.Direction.ASC, "sortOrder", "name")))
                .map(this::toStandardResponse);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public List<StandardResponse> listStandardOptions() {
        return standardRepository.findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::toStandardResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BoardOptionResponse> listBoards() {
        return Arrays.stream(BoardType.values())
                .map(board -> BoardOptionResponse.builder()
                        .code(board)
                        .label(board.getDisplayName())
                        .build())
                .toList();
    }

    @Transactional
    public StandardResponse createStandard(CreateStandardRequest request) {
        String standardName = normalizeStandard(request.getStandard());
        if (standardRepository.existsByNameIgnoreCaseAndBoardAndIsDeletedFalse(standardName, request.getBoard())) {
            throw new ResourceAlreadyExistsException("This standard and board combination already exists.");
        }

        Standard standard = Standard.builder()
                .code(nextCode("STD", standardRepository.count() + 1, candidate -> standardRepository.existsByCodeAndIsDeletedFalse(candidate)))
                .name(standardName)
                .board(request.getBoard())
                .sortOrder((int) standardRepository.count())
                .isActive(true)
                .build();
        return toStandardResponse(standardRepository.save(standard));
    }

    @Transactional
    public StandardResponse updateStandard(UUID standardId, UpdateStandardRequest request) {
        Standard standard = standardRepository.findByIdAndIsDeletedFalse(standardId)
                .orElseThrow(() -> new ResourceNotFoundException("Standard", standardId));

        String standardName = normalizeStandard(request.getStandard());
        standardRepository.findByNameIgnoreCaseAndBoardAndIsDeletedFalse(standardName, request.getBoard())
                .filter(existing -> !existing.getId().equals(standardId))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("This standard and board combination already exists.");
                });

        standard.setName(standardName);
        standard.setBoard(request.getBoard());
        return toStandardResponse(standardRepository.save(standard));
    }

    @Transactional
    public void deleteStandard(UUID standardId) {
        Standard standard = standardRepository.findByIdAndIsDeletedFalse(standardId)
                .orElseThrow(() -> new ResourceNotFoundException("Standard", standardId));
        standard.softDelete();
        standard.setIsActive(false);
        standardRepository.save(standard);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AcademicCourseListItemResponse> listCourses(UUID branchId, UUID currentBranchId, String search, int page, int size) {
        UUID resolvedBranchId = resolveBranchIdForRead(branchId, currentBranchId);
        Page<AcademicCourseListItemResponse> result = batchRepository.searchAcademicCourses(
                        resolvedBranchId,
                        buildSearchPattern(search),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayCode")))
                .map(this::toAcademicCourseListItemResponse);
        return PagedResponse.from(result);
    }

    @Transactional
    public AcademicCourseDetailResponse createCourse(UUID branchId, UUID currentBranchId, CreateAcademicCourseRequest request) {
        Standard standard = resolveStandard(request.getStandard(), request.getBoard());
        Branch branch = resolveBranchForWrite(branchId, currentBranchId);
        validateTimeRange(request.getStartTime(), request.getEndTime());

        Course course = Course.builder()
                .name(normalizeName(request.getCourseName(), "Course name is required"))
                .code(UUID.randomUUID().toString())
                .standardRef(standard)
                .category(resolveCategory(request.getBoard()))
                .board(request.getBoard())
                .standard(standard.getName())
                .academicYear(normalizeName(request.getAcademicYear(), "Academic year is required"))
                .medium(normalizeName(request.getMedium(), "Medium is required"))
                .isActive(true)
                .build();
        course = courseRepository.save(course);

        Batch batch = Batch.builder()
                .branch(branch)
                .course(course)
                .displayCode(nextCode("CRS", batchRepository.count() + 1, batchRepository::existsByDisplayCode))
                .name(normalizeName(request.getBatchName(), "Batch name is required"))
                .academicYear(course.getAcademicYear())
                .timing(request.getBatchTiming())
                .timingLabel(buildTimingLabel(request.getStartTime(), request.getEndTime()))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(true)
                .build();

        Batch savedBatch = batchRepository.save(batch);
        return toAcademicCourseDetailResponse(loadAcademicBatch(savedBatch.getId()));
    }

    @Transactional(readOnly = true)
    public AcademicCourseDetailResponse getCourse(UUID id, UUID branchId, UUID currentBranchId) {
        Batch batch = loadAcademicBatch(id);
        ensureBranchAccess(batch, branchId, currentBranchId);
        return toAcademicCourseDetailResponse(batch);
    }

    @Transactional
    public AcademicCourseDetailResponse updateCourse(UUID id, UUID branchId, UUID currentBranchId, UpdateAcademicCourseRequest request) {
        Batch batch = loadAcademicBatch(id);
        ensureBranchAccess(batch, branchId, currentBranchId);
        Standard standard = resolveStandard(request.getStandard(), request.getBoard());
        validateTimeRange(request.getStartTime(), request.getEndTime());

        Course course = batch.getCourse();
        course.setStandardRef(standard);
        course.setBoard(request.getBoard());
        course.setStandard(standard.getName());
        course.setMedium(normalizeName(request.getMedium(), "Medium is required"));
        course.setAcademicYear(normalizeName(request.getAcademicYear(), "Academic year is required"));
        course.setName(normalizeName(request.getCourseName(), "Course name is required"));

        batch.setName(normalizeName(request.getBatchName(), "Batch name is required"));
        batch.setAcademicYear(course.getAcademicYear());
        batch.setTiming(request.getBatchTiming());
        batch.setTimingLabel(buildTimingLabel(request.getStartTime(), request.getEndTime()));
        batch.setStartTime(request.getStartTime());
        batch.setEndTime(request.getEndTime());

        courseRepository.save(course);
        batchRepository.save(batch);
        return toAcademicCourseDetailResponse(loadAcademicBatch(id));
    }

    @Transactional
    public void deleteCourse(UUID id, UUID branchId, UUID currentBranchId) {
        Batch batch = loadAcademicBatch(id);
        ensureBranchAccess(batch, branchId, currentBranchId);
        batch.softDelete();
        batch.setIsActive(false);
        batchRepository.save(batch);

        Course course = batch.getCourse();
        course.softDelete();
        course.setIsActive(false);
        courseRepository.save(course);
    }

    @Transactional
    public CourseSubjectResponse addSubject(UUID courseId, UUID branchId, UUID currentBranchId, CreateCourseSubjectRequest request) {
        Batch batch = loadAcademicBatch(courseId);
        ensureBranchAccess(batch, branchId, currentBranchId);

        Course course = batch.getCourse();
        String subjectName = normalizeName(request.getSubjectName(), "Subject name is required");
        boolean alreadyAttached = course.getSubjects().stream()
                .anyMatch(subject -> subjectName.equalsIgnoreCase(subject.getDisplayName()));
        if (alreadyAttached) {
            throw new ResourceAlreadyExistsException("This subject is already added to the course.");
        }

        Subject subject = Subject.builder()
                .displayCode(nextCode("SUB", subjectRepository.count() + 1, subjectRepository::existsByDisplayCodeAndIsDeletedFalse))
                .code(generateSubjectCode(subjectName))
                .displayName(subjectName)
                .shortName(subjectName)
                .isActive(true)
                .build();

        if (subjectRepository.existsByCodeIgnoreCaseAndIsDeletedFalse(subject.getCode())) {
            subject.setCode(subject.getCode() + "_" + subject.getDisplayCode());
        }

        Subject savedSubject = subjectRepository.save(subject);
        course.getSubjects().add(savedSubject);
        courseRepository.save(course);
        return toCourseSubjectResponse(savedSubject);
    }

    @Transactional
    public CourseSubjectResponse updateSubject(UUID courseId, UUID subjectId, UUID branchId, UUID currentBranchId, UpdateCourseSubjectRequest request) {
        Batch batch = loadAcademicBatch(courseId);
        ensureBranchAccess(batch, branchId, currentBranchId);
        Course course = batch.getCourse();
        Subject subject = course.getSubjects().stream()
                .filter(item -> item.getId().equals(subjectId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));

        subject.setDisplayName(normalizeName(request.getSubjectName(), "Subject name is required"));
        subject.setShortName(subject.getDisplayName());
        subjectRepository.save(subject);
        return toCourseSubjectResponse(subject);
    }

    @Transactional
    public void deleteSubject(UUID courseId, UUID subjectId, UUID branchId, UUID currentBranchId) {
        Batch batch = loadAcademicBatch(courseId);
        ensureBranchAccess(batch, branchId, currentBranchId);
        Course course = batch.getCourse();
        Subject subject = course.getSubjects().stream()
                .filter(item -> item.getId().equals(subjectId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        course.getSubjects().remove(subject);
        courseRepository.save(course);
        subject.softDelete();
        subject.setIsActive(false);
        subjectRepository.save(subject);
    }

    private StandardResponse toStandardResponse(Standard standard) {
        return StandardResponse.builder()
                .id(standard.getId())
                .standardId(standard.getCode())
                .standard(standard.getName())
                .board(standard.getBoard())
                .boardLabel(standard.getBoard().getDisplayName())
                .status(Boolean.TRUE.equals(standard.getIsActive()) ? "Active" : "Inactive")
                .build();
    }

    private AcademicCourseListItemResponse toAcademicCourseListItemResponse(Batch batch) {
        Course course = batch.getCourse();
        return AcademicCourseListItemResponse.builder()
                .id(batch.getId())
                .courseId(resolveCourseCode(batch))
                .standard(course.getStandard())
                .board(course.getBoard() != null ? course.getBoard().getDisplayName() : null)
                .medium(course.getMedium())
                .academicYear(course.getAcademicYear())
                .courseName(course.getName())
                .batchName(batch.getName())
                .batchTiming(buildTimingLabel(batch.getStartTime(), batch.getEndTime()))
                .build();
    }

    private AcademicCourseDetailResponse toAcademicCourseDetailResponse(Batch batch) {
        Course course = batch.getCourse();
        List<CourseSubjectResponse> subjects = course.getSubjects() == null ? List.of() : course.getSubjects().stream()
                .filter(subject -> !subject.isDeleted())
                .sorted(Comparator.comparing(Subject::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCourseSubjectResponse)
                .toList();

        return AcademicCourseDetailResponse.builder()
                .id(batch.getId())
                .courseId(String.valueOf(batch.getCourse().getId()))
                .standard(course.getStandard())
                .board(course.getBoard() != null ? course.getBoard().getDisplayName() : null)
                .medium(course.getMedium())
                .academicYear(course.getAcademicYear())
                .courseName(course.getName())
                .batchName(batch.getName())
                .batchTiming(buildTimingLabel(batch.getStartTime(), batch.getEndTime()))
                .startTime(batch.getStartTime())
                .endTime(batch.getEndTime())
                .subjects(subjects)
                .build();
    }

    private CourseSubjectResponse toCourseSubjectResponse(Subject subject) {
        return CourseSubjectResponse.builder()
                .id(subject.getId())
                .subjectId(StringUtils.hasText(subject.getDisplayCode()) ? subject.getDisplayCode() : "SUB-" + subject.getId())
                .subjectName(subject.getDisplayName())
                .subjectCode(subject.getCode())
                .status(Boolean.TRUE.equals(subject.getIsActive()) ? "Active" : "Inactive")
                .build();
    }

    private Standard resolveStandard(String standardName, BoardType board) {
        return standardRepository.findByNameIgnoreCaseAndBoardAndIsDeletedFalse(normalizeStandard(standardName), board)
                .orElseThrow(() -> new BadRequestException("Selected standard and board do not exist.", "STANDARD_NOT_FOUND"));
    }

    private Branch resolveBranchForWrite(UUID branchId, UUID currentBranchId) {
        UUID resolvedId = branchId != null ? branchId : currentBranchId;
        if (resolvedId != null) {
            return branchRepository.findByIdAndIsDeletedFalse(resolvedId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", resolvedId));
        }

        return branchRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc().stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No active branch available for course creation.", "BRANCH_REQUIRED"));
    }

    private UUID resolveBranchIdForRead(UUID branchId, UUID currentBranchId) {
        return branchId != null ? branchId : currentBranchId;
    }

    private void ensureBranchAccess(Batch batch, UUID branchId, UUID currentBranchId) {
        UUID expectedBranchId = branchId != null ? branchId : currentBranchId;
        if (expectedBranchId != null && !expectedBranchId.equals(batch.getBranch().getId())) {
            throw new ResourceNotFoundException("Course", batch.getId());
        }
    }

    private Batch loadAcademicBatch(UUID batchId) {
        return batchRepository.findAcademicCourseDetail(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", batchId));
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

    private String normalizeStandard(String value) {
        return normalizeName(value, "Class / standard is required");
    }

    private String normalizeName(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message, "VALIDATION_ERROR");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String buildSearchPattern(String value) {
        return StringUtils.hasText(value) ? "%" + value.trim().toLowerCase(Locale.ENGLISH) + "%" : null;
    }

    private String resolveCourseCode(Batch batch) {
        return StringUtils.hasText(batch.getDisplayCode()) ? batch.getDisplayCode() : "CRS-" + batch.getId();
    }

    private String nextCode(String prefix, long startAt, java.util.function.Predicate<String> exists) {
        long index = Math.max(startAt, 1);
        String candidate;
        do {
            candidate = prefix + String.format("%04d", index);
            index++;
        } while (exists.test(candidate));
        return candidate;
    }

    private String generateSubjectCode(String subjectName) {
        String normalized = subjectName.toUpperCase(Locale.ENGLISH).replaceAll("[^A-Z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        if (!StringUtils.hasText(normalized)) {
            normalized = "SUBJECT";
        }
        return normalized.length() > 40 ? normalized.substring(0, 40) : normalized;
    }
}
