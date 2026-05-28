package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateStudentRequest;
import com.classmanager.cms_backend.dto.request.GenerateStudentPasswordRequest;
import com.classmanager.cms_backend.dto.request.UpdateStudentRequest;
import com.classmanager.cms_backend.dto.request.UpdateStudentStatusRequest;
import com.classmanager.cms_backend.dto.response.StudentManagementResponse;
import com.classmanager.cms_backend.dto.response.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentManagementService studentManagementService;

    public StudentManagementResponse getStudents(String search, Boolean isActive, UUID branchId, String standard, String batch, int page, int size) {
        return studentManagementService.getStudents(search, isActive, branchId, standard, batch, page, size);
    }

    public StudentResponse getStudent(UUID studentId) {
        return studentManagementService.getStudent(studentId);
    }

    public StudentResponse createStudent(CreateStudentRequest request, UUID createdByUserId) {
        return studentManagementService.createStudent(request, createdByUserId);
    }

    public StudentResponse updateStudent(UUID studentId, UpdateStudentRequest request, UUID updatedByUserId) {
        return studentManagementService.updateStudent(studentId, request, updatedByUserId);
    }

    public StudentResponse updateStudentStatus(UUID studentId, UpdateStudentStatusRequest request, UUID updatedByUserId) {
        return studentManagementService.updateStudentStatus(studentId, request, updatedByUserId);
    }

    public StudentResponse generateStudentPassword(UUID studentId, GenerateStudentPasswordRequest request, UUID actorUserId) {
        return studentManagementService.generateStudentPassword(studentId, request, actorUserId);
    }

    public void deleteStudent(UUID studentId, UUID deletedByUserId) {
        studentManagementService.deleteStudent(studentId, deletedByUserId);
    }
}
