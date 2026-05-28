package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateEnrolmentRequest;
import com.classmanager.cms_backend.dto.request.UpdateEnrolmentRequest;
import com.classmanager.cms_backend.dto.response.StudentEnrolmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrolmentService {

    private final StudentEnrolmentService studentEnrolmentService;

    public StudentEnrolmentResponse createEnrolment(CreateEnrolmentRequest request) {
        return studentEnrolmentService.createEnrolment(request);
    }

    public StudentEnrolmentResponse getEnrolment(UUID enrolmentId) {
        return studentEnrolmentService.getEnrolment(enrolmentId);
    }

    public List<StudentEnrolmentResponse> getEnrolmentsByStudent(UUID studentId) {
        return studentEnrolmentService.getEnrolmentsByStudent(studentId);
    }

    public Page<StudentEnrolmentResponse> getEnrolmentsByBatch(UUID batchId, Pageable pageable) {
        return studentEnrolmentService.getEnrolmentsByBatch(batchId, pageable);
    }

    public Page<StudentEnrolmentResponse> getEnrolmentsByBranchAndYear(UUID branchId, String academicYear, Pageable pageable) {
        return studentEnrolmentService.getEnrolmentsByBranchAndYear(branchId, academicYear, pageable);
    }

    public StudentEnrolmentResponse updateEnrolment(UUID enrolmentId, UpdateEnrolmentRequest request) {
        return studentEnrolmentService.updateEnrolment(enrolmentId, request);
    }

    public void deleteEnrolment(UUID enrolmentId) {
        studentEnrolmentService.deleteEnrolment(enrolmentId);
    }
}
