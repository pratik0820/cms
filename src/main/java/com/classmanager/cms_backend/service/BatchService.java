package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateBatchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBatchRequest;
import com.classmanager.cms_backend.dto.response.BatchResponse;
import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Teacher;
import com.classmanager.cms_backend.enums.BatchTiming;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BatchRepository;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.CourseRepository;
import com.classmanager.cms_backend.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BatchService {

    private static final Logger log = LogManager.getLogger(BatchService.class);

    private final BatchRepository batchRepository;
    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public BatchResponse createBatch(CreateBatchRequest request) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));

        Course course = courseRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        if (request.getTiming() == BatchTiming.CUSTOM && !StringUtils.hasText(request.getTimingLabel())) {
            throw new BadRequestException("timingLabel is required when timing is CUSTOM", "TIMING_LABEL_REQUIRED");
        }

        Teacher classTeacher = null;
        if (request.getClassTeacherId() != null) {
            classTeacher = teacherRepository.findByIdAndIsDeletedFalse(request.getClassTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", request.getClassTeacherId()));
        }

        String batchName = StringUtils.hasText(request.getName())
                ? request.getName()
                : buildBatchName(course, branch, request);

        Batch batch = Batch.builder()
                .branch(branch)
                .course(course)
                .name(batchName)
                .academicYear(request.getAcademicYear())
                .batchType(request.getBatchType())
                .timing(request.getTiming())
                .timingLabel(request.getTimingLabel())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .daysOfWeek(request.getDaysOfWeek())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 0)
                .classTeacher(classTeacher)
                .room(request.getRoom())
                .build();

        batch = batchRepository.save(batch);
        log.info("Batch created: id={} name={} branch={}", batch.getId(), batch.getName(), branch.getName());
        return toBatchResponse(batch);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> listBatches(UUID branchId, Pageable pageable) {
        Page<Batch> page = branchId != null
                ? batchRepository.findByBranch_IdAndIsDeletedFalseOrderByNameAsc(branchId, pageable)
                : batchRepository.findByIsDeletedFalseOrderByNameAsc(pageable);
        return page.map(this::toBatchResponse);
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> listBatchesByBranch(UUID branchId) {
        return batchRepository.findByBranch_IdAndIsActiveTrueAndIsDeletedFalseOrderByNameAsc(branchId)
                .stream().map(this::toBatchResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> listBatchesByBranchAndYear(UUID branchId, String academicYear) {
        return batchRepository.findByBranch_IdAndAcademicYearAndIsActiveTrueAndIsDeletedFalseOrderByNameAsc(branchId, academicYear)
                .stream().map(this::toBatchResponse).toList();
    }

    @Transactional(readOnly = true)
    public BatchResponse getBatch(UUID batchId) {
        Batch batch = batchRepository.findByIdAndIsDeletedFalse(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));
        return toBatchResponse(batch);
    }

    @Transactional
    public BatchResponse updateBatch(UUID batchId, UpdateBatchRequest request) {
        Batch batch = batchRepository.findByIdAndIsDeletedFalse(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));

        if (request.getName() != null) batch.setName(request.getName());
        if (request.getBatchType() != null) batch.setBatchType(request.getBatchType());
        if (request.getTiming() != null) batch.setTiming(request.getTiming());
        if (request.getTimingLabel() != null) batch.setTimingLabel(request.getTimingLabel());
        if (request.getStartTime() != null) batch.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) batch.setEndTime(request.getEndTime());
        if (request.getDaysOfWeek() != null) batch.setDaysOfWeek(request.getDaysOfWeek());
        if (request.getStartDate() != null) batch.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) batch.setEndDate(request.getEndDate());
        if (request.getMaxStudents() != null) batch.setMaxStudents(request.getMaxStudents());
        if (request.getRoom() != null) batch.setRoom(request.getRoom());
        if (request.getIsActive() != null) batch.setIsActive(request.getIsActive());

        if (request.getClassTeacherId() != null) {
            Teacher teacher = teacherRepository.findByIdAndIsDeletedFalse(request.getClassTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", request.getClassTeacherId()));
            batch.setClassTeacher(teacher);
        }

        batch = batchRepository.save(batch);
        return toBatchResponse(batch);
    }

    @Transactional
    public void deleteBatch(UUID batchId) {
        Batch batch = batchRepository.findByIdAndIsDeletedFalse(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));
        batch.softDelete();
        batchRepository.save(batch);
        log.info("Batch soft-deleted: id={}", batchId);
    }

    public BatchResponse toBatchResponse(Batch b) {
        return BatchResponse.builder()
                .id(b.getId())
                .branchId(b.getBranch().getId())
                .branchName(b.getBranch().getName())
                .courseId(b.getCourse().getId())
                .courseName(b.getCourse().getName())
                .courseCode(b.getCourse().getCode())
                .name(b.getName())
                .academicYear(b.getAcademicYear())
                .batchType(b.getBatchType())
                .timing(b.getTiming())
                .timingLabel(b.getTimingLabel())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .daysOfWeek(b.getDaysOfWeek())
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .maxStudents(b.getMaxStudents())
                .enrolledCount(b.getEnrolledCount())
                .classTeacherId(b.getClassTeacher() != null ? b.getClassTeacher().getId() : null)
                .classTeacherName(b.getClassTeacher() != null ? b.getClassTeacher().getName() : null)
                .room(b.getRoom())
                .isActive(b.getIsActive())
                .build();
    }

    private String buildBatchName(Course course, Branch branch, CreateBatchRequest req) {
        String timing = req.getTiming() == BatchTiming.CUSTOM && StringUtils.hasText(req.getTimingLabel())
                ? req.getTimingLabel()
                : req.getTiming().name();
        return course.getName() + " – " + timing + " – " + req.getAcademicYear() + " (" + branch.getName() + ")";
    }
}
