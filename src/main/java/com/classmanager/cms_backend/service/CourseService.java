package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.request.UpdateAcademicCourseRequest;
import com.classmanager.cms_backend.dto.response.AcademicCourseDetailResponse;
import com.classmanager.cms_backend.dto.response.AcademicCourseListItemResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final AcademicManagementService academicManagementService;

    public PagedResponse<AcademicCourseListItemResponse> listCourses(UUID branchId, UUID currentBranchId, String search, int page, int size) {
        return academicManagementService.listCourses(branchId, currentBranchId, search, page, size);
    }

    public AcademicCourseDetailResponse createCourse(UUID branchId, UUID currentBranchId, CreateAcademicCourseRequest request) {
        return academicManagementService.createCourse(branchId, currentBranchId, request);
    }

    public AcademicCourseDetailResponse getCourse(UUID courseId, UUID branchId, UUID currentBranchId) {
        return academicManagementService.getCourse(courseId, branchId, currentBranchId);
    }

    public AcademicCourseDetailResponse updateCourse(UUID courseId, UUID branchId, UUID currentBranchId, UpdateAcademicCourseRequest request) {
        return academicManagementService.updateCourse(courseId, branchId, currentBranchId, request);
    }

    public void deleteCourse(UUID courseId, UUID branchId, UUID currentBranchId) {
        academicManagementService.deleteCourse(courseId, branchId, currentBranchId);
    }
}
