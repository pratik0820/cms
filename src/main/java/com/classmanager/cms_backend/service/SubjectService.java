package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.request.UpdateCourseSubjectRequest;
import com.classmanager.cms_backend.dto.response.CourseSubjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final AcademicManagementService academicManagementService;

    public List<CourseSubjectResponse> listSubjects() {
        return academicManagementService.listGlobalSubjects();
    }

    public CourseSubjectResponse addCourseSubject(UUID courseId, UUID branchId, UUID currentBranchId, CreateCourseSubjectRequest request) {
        return academicManagementService.addSubject(courseId, branchId, currentBranchId, request);
    }

    public CourseSubjectResponse updateCourseSubject(UUID courseId, UUID subjectId, UUID branchId, UUID currentBranchId, UpdateCourseSubjectRequest request) {
        return academicManagementService.updateSubject(courseId, subjectId, branchId, currentBranchId, request);
    }

    public void deleteCourseSubject(UUID courseId, UUID subjectId, UUID branchId, UUID currentBranchId) {
        academicManagementService.deleteSubject(courseId, subjectId, branchId, currentBranchId);
    }
}
