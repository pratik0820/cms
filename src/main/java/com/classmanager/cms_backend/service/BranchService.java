package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateBranchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBranchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBranchStatusRequest;
import com.classmanager.cms_backend.dto.response.BranchManagementResponse;
import com.classmanager.cms_backend.dto.response.BranchResponse;
import com.classmanager.cms_backend.dto.response.SuperAdminDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final SuperAdminBranchService superAdminBranchService;

    public List<SuperAdminDashboardResponse.BranchOption> getBranchOptions() {
        return superAdminBranchService.getBranchOptions();
    }

    public BranchManagementResponse getBranches(String search, Boolean isActive, int page, int size) {
        return superAdminBranchService.getBranches(search, isActive, page, size);
    }

    public BranchResponse getBranch(UUID branchId) {
        return superAdminBranchService.getBranch(branchId);
    }

    public BranchResponse createBranch(CreateBranchRequest request, UUID createdByUserId) {
        return superAdminBranchService.createBranch(request, createdByUserId);
    }

    public BranchResponse updateBranch(UUID branchId, UpdateBranchRequest request, UUID updatedByUserId) {
        return superAdminBranchService.updateBranch(branchId, request, updatedByUserId);
    }

    public BranchResponse updateBranchStatus(UUID branchId, UpdateBranchStatusRequest request, UUID updatedByUserId) {
        return superAdminBranchService.updateBranchStatus(branchId, request, updatedByUserId);
    }

    public void deleteBranch(UUID branchId, UUID deletedByUserId) {
        superAdminBranchService.deleteBranch(branchId, deletedByUserId);
    }
}
