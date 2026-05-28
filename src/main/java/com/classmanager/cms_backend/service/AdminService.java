package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateAdminRequest;
import com.classmanager.cms_backend.dto.request.UpdateAdminRequest;
import com.classmanager.cms_backend.dto.request.UpdateAdminStatusRequest;
import com.classmanager.cms_backend.dto.response.AdminManagementResponse;
import com.classmanager.cms_backend.dto.response.AdminResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SuperAdminAdminService superAdminAdminService;

    public AdminManagementResponse getAdmins(String search, Boolean isActive, UUID branchId, int page, int size) {
        return superAdminAdminService.getAdmins(search, isActive, branchId, page, size);
    }

    public AdminResponse getAdmin(UUID adminId) {
        return superAdminAdminService.getAdmin(adminId);
    }

    public AdminResponse createAdmin(CreateAdminRequest request, UUID createdByUserId) {
        return superAdminAdminService.createAdmin(request, createdByUserId);
    }

    public AdminResponse updateAdmin(UUID adminId, UpdateAdminRequest request, UUID updatedByUserId) {
        return superAdminAdminService.updateAdmin(adminId, request, updatedByUserId);
    }

    public AdminResponse updateAdminStatus(UUID adminId, UpdateAdminStatusRequest request, UUID updatedByUserId) {
        return superAdminAdminService.updateAdminStatus(adminId, request, updatedByUserId);
    }

    public void deleteAdmin(UUID adminId, UUID deletedByUserId) {
        superAdminAdminService.deleteAdmin(adminId, deletedByUserId);
    }
}
