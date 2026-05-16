package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateEnrolmentRequest;
import com.classmanager.cms_backend.dto.request.UpdateEnrolmentRequest;
import com.classmanager.cms_backend.dto.response.EnrolmentInstalmentResponse;
import com.classmanager.cms_backend.dto.response.StudentEnrolmentResponse;
import com.classmanager.cms_backend.dto.response.SubjectResponse;
import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.EnrolmentInstalment;
import com.classmanager.cms_backend.entity.Student;
import com.classmanager.cms_backend.entity.StudentEnrolment;
import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.entity.SubjectGroup;
import com.classmanager.cms_backend.enums.EnrolmentStatus;
import com.classmanager.cms_backend.exception.BusinessRuleException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BatchRepository;
import com.classmanager.cms_backend.repository.StudentEnrolmentRepository;
import com.classmanager.cms_backend.repository.StudentRepository;
import com.classmanager.cms_backend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentEnrolmentService {

    private static final Logger log = LogManager.getLogger(StudentEnrolmentService.class);

    private final StudentEnrolmentRepository enrolmentRepository;
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final SubjectRepository subjectRepository;
    private final CourseService courseService;

    @Transactional
    public StudentEnrolmentResponse createEnrolment(CreateEnrolmentRequest request) {
        Student student = studentRepository.findByIdAndIsDeletedFalse(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));

        Batch batch = batchRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch", request.getBatchId()));

        // Prevent duplicate active enrolment in the same batch
        if (enrolmentRepository.existsByStudent_IdAndBatch_IdAndStatusAndIsDeletedFalse(
                student.getId(), batch.getId(), EnrolmentStatus.ACTIVE)) {
            throw new BusinessRuleException(
                    "Student is already actively enrolled in this batch.",
                    "DUPLICATE_ENROLMENT");
        }

        // Resolve optional subject group
        SubjectGroup subjectGroup = null;
        if (request.getSubjectGroupId() != null) {
            subjectGroup = batch.getCourse().getSubjectGroups().stream()
                    .filter(sg -> sg.getId().equals(request.getSubjectGroupId())
                            && !sg.isDeleted()
                            && Boolean.TRUE.equals(sg.getIsActive()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("SubjectGroup", request.getSubjectGroupId()));
        }

        // Resolve subjects
        List<Subject> subjects = resolveSubjects(request.getSubjectIds());

        // Build enrolment
        StudentEnrolment enrolment = StudentEnrolment.builder()
                .student(student)
                .batch(batch)
                .academicYear(batch.getAcademicYear())
                .subjectGroup(subjectGroup)
                .subjects(subjects)
                .agreedTotalFee(request.getAgreedTotalFee())
                .paymentPlan(request.getPaymentPlan())
                .enrolmentDate(request.getEnrolmentDate())
                .notes(request.getNotes())
                .status(EnrolmentStatus.ACTIVE)
                .build();

        // Build instalment schedule
        List<EnrolmentInstalment> instalments = buildInstalments(enrolment, request.getInstalments());
        enrolment.setInstalments(instalments);

        enrolment = enrolmentRepository.save(enrolment);

        // Update batch enrolled count
        updateBatchEnrolledCount(batch);

        log.info("Enrolment created: id={} student={} batch={}", enrolment.getId(),
                student.getName(), batch.getName());
        return toResponse(enrolment);
    }

    @Transactional(readOnly = true)
    public StudentEnrolmentResponse getEnrolment(UUID enrolmentId) {
        StudentEnrolment enrolment = enrolmentRepository.findByIdAndIsDeletedFalse(enrolmentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentEnrolment", enrolmentId));
        return toResponse(enrolment);
    }

    @Transactional(readOnly = true)
    public List<StudentEnrolmentResponse> getEnrolmentsByStudent(UUID studentId) {
        return enrolmentRepository
                .findByStudent_IdAndIsDeletedFalseOrderByEnrolmentDateDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<StudentEnrolmentResponse> getEnrolmentsByBatch(UUID batchId, Pageable pageable) {
        return enrolmentRepository
                .findByBatch_IdAndIsDeletedFalseOrderByStudent_NameAsc(batchId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<StudentEnrolmentResponse> getEnrolmentsByBranchAndYear(
            UUID branchId, String academicYear, Pageable pageable) {
        return enrolmentRepository
                .findByBranchAndYear(branchId, academicYear, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public StudentEnrolmentResponse updateEnrolment(UUID enrolmentId, UpdateEnrolmentRequest request) {
        StudentEnrolment enrolment = enrolmentRepository.findByIdAndIsDeletedFalse(enrolmentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentEnrolment", enrolmentId));

        if (request.getSubjectIds() != null) {
            enrolment.setSubjects(resolveSubjects(request.getSubjectIds()));
        }

        if (request.getSubjectGroupId() != null) {
            SubjectGroup sg = enrolment.getBatch().getCourse().getSubjectGroups().stream()
                    .filter(g -> g.getId().equals(request.getSubjectGroupId())
                            && !g.isDeleted()
                            && Boolean.TRUE.equals(g.getIsActive()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("SubjectGroup", request.getSubjectGroupId()));
            enrolment.setSubjectGroup(sg);
        }

        if (request.getAgreedTotalFee() != null) enrolment.setAgreedTotalFee(request.getAgreedTotalFee());
        if (request.getPaymentPlan() != null) enrolment.setPaymentPlan(request.getPaymentPlan());
        if (request.getEnrolmentDate() != null) enrolment.setEnrolmentDate(request.getEnrolmentDate());
        if (request.getNotes() != null) enrolment.setNotes(request.getNotes());

        if (request.getStatus() != null) {
            EnrolmentStatus oldStatus = enrolment.getStatus();
            enrolment.setStatus(request.getStatus());
            if (oldStatus != request.getStatus()) {
                updateBatchEnrolledCount(enrolment.getBatch());
            }
        }

        if (request.getInstalments() != null) {
            enrolment.getInstalments().clear();
            enrolment.getInstalments().addAll(buildInstalments(enrolment, request.getInstalments()));
        }

        enrolment = enrolmentRepository.save(enrolment);
        return toResponse(enrolment);
    }

    @Transactional
    public void deleteEnrolment(UUID enrolmentId) {
        StudentEnrolment enrolment = enrolmentRepository.findByIdAndIsDeletedFalse(enrolmentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentEnrolment", enrolmentId));
        enrolment.softDelete();
        enrolmentRepository.save(enrolment);
        updateBatchEnrolledCount(enrolment.getBatch());
        log.info("Enrolment soft-deleted: id={}", enrolmentId);
    }

    private List<Subject> resolveSubjects(List<UUID> subjectIds) {
        List<Subject> subjects = new ArrayList<>();
        for (UUID id : subjectIds) {
            subjects.add(subjectRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject", id)));
        }
        return subjects;
    }

    private List<EnrolmentInstalment> buildInstalments(
            StudentEnrolment enrolment,
            List<CreateEnrolmentRequest.InstalmentRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }
        List<EnrolmentInstalment> result = new ArrayList<>();
        for (CreateEnrolmentRequest.InstalmentRequest r : requests) {
            result.add(EnrolmentInstalment.builder()
                    .enrolment(enrolment)
                    .instalmentNumber(r.getInstalmentNumber())
                    .label(r.getLabel())
                    .amount(r.getAmount())
                    .dueDate(r.getDueDate())
                    .isPostDatedCheque(r.getIsPostDatedCheque() != null && r.getIsPostDatedCheque())
                    .build());
        }
        return result;
    }

    private void updateBatchEnrolledCount(Batch batch) {
        long count = enrolmentRepository.countByBatch_IdAndStatusAndIsDeletedFalse(
                batch.getId(), EnrolmentStatus.ACTIVE);
        batch.setEnrolledCount((int) count);
        batchRepository.save(batch);
    }

    private StudentEnrolmentResponse toResponse(StudentEnrolment e) {
        BigDecimal totalPaid = e.getInstalments().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPaid()) && !i.isDeleted())
                .map(EnrolmentInstalment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPending = e.getAgreedTotalFee().subtract(totalPaid);

        List<SubjectResponse> subjectResponses = e.getSubjects().stream()
                .map(courseService::toSubjectResponse)
                .toList();

        List<EnrolmentInstalmentResponse> instalmentResponses = e.getInstalments().stream()
                .filter(i -> !i.isDeleted())
                .map(i -> EnrolmentInstalmentResponse.builder()
                        .id(i.getId())
                        .instalmentNumber(i.getInstalmentNumber())
                        .label(i.getLabel())
                        .amount(i.getAmount())
                        .dueDate(i.getDueDate())
                        .isPostDatedCheque(i.getIsPostDatedCheque())
                        .isPaid(i.getIsPaid())
                        .paidDate(i.getPaidDate())
                        .build())
                .toList();

        return StudentEnrolmentResponse.builder()
                .id(e.getId())
                .studentId(e.getStudent().getId())
                .studentName(e.getStudent().getName())
                .studentLoginId(e.getStudent().getUser() != null ? e.getStudent().getUser().getLoginId() : null)
                .batchId(e.getBatch().getId())
                .batchName(e.getBatch().getName())
                .academicYear(e.getAcademicYear())
                .courseId(e.getBatch().getCourse().getId())
                .courseName(e.getBatch().getCourse().getName())
                .courseCode(e.getBatch().getCourse().getCode())
                .subjectGroupId(e.getSubjectGroup() != null ? e.getSubjectGroup().getId() : null)
                .subjectGroupName(e.getSubjectGroup() != null ? e.getSubjectGroup().getName() : null)
                .subjects(subjectResponses)
                .agreedTotalFee(e.getAgreedTotalFee())
                .paymentPlan(e.getPaymentPlan())
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .instalments(instalmentResponses)
                .enrolmentDate(e.getEnrolmentDate())
                .status(e.getStatus())
                .notes(e.getNotes())
                .build();
    }
}
