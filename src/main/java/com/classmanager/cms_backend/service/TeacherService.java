package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateTeacherRequest;
import com.classmanager.cms_backend.dto.request.UpdateTeacherRequest;
import com.classmanager.cms_backend.dto.request.UpdateTeacherStatusRequest;
import com.classmanager.cms_backend.dto.response.TeacherManagementResponse;
import com.classmanager.cms_backend.dto.response.TeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final SuperAdminTeacherService superAdminTeacherService;

    public TeacherManagementResponse getTeachers(String search, Boolean isActive, UUID branchId, String subject,
                                                 UUID subjectId, UUID courseId, UUID batchId, int page, int size) {
        return superAdminTeacherService.getTeachers(search, isActive, branchId, subject, subjectId, courseId, batchId, page, size);
    }

    public TeacherResponse getTeacher(UUID teacherId) {
        return superAdminTeacherService.getTeacher(teacherId);
    }

    public TeacherResponse createTeacher(CreateTeacherRequest request, UUID createdByUserId) {
        return superAdminTeacherService.createTeacher(request, createdByUserId);
    }

    public TeacherResponse updateTeacher(UUID teacherId, UpdateTeacherRequest request, UUID updatedByUserId) {
        return superAdminTeacherService.updateTeacher(teacherId, request, updatedByUserId);
    }

    public TeacherResponse updateTeacherStatus(UUID teacherId, UpdateTeacherStatusRequest request, UUID updatedByUserId) {
        return superAdminTeacherService.updateTeacherStatus(teacherId, request, updatedByUserId);
    }

    public void deleteTeacher(UUID teacherId, UUID deletedByUserId) {
        superAdminTeacherService.deleteTeacher(teacherId, deletedByUserId);
    }
}
