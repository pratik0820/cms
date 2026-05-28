package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateStandardRequest;
import com.classmanager.cms_backend.dto.request.UpdateStandardRequest;
import com.classmanager.cms_backend.dto.response.BoardOptionResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.dto.response.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StandardService {

    private final AcademicManagementService academicManagementService;

    public List<BoardOptionResponse> listBoards() {
        return academicManagementService.listBoards();
    }

    public PagedResponse<StandardResponse> listStandards(String search, int page, int size) {
        return academicManagementService.listStandards(search, page, size);
    }

    public List<StandardResponse> listStandardOptions() {
        return academicManagementService.listStandardOptions();
    }

    public StandardResponse createStandard(CreateStandardRequest request) {
        return academicManagementService.createStandard(request);
    }

    public StandardResponse updateStandard(UUID standardId, UpdateStandardRequest request) {
        return academicManagementService.updateStandard(standardId, request);
    }

    public void deleteStandard(UUID standardId) {
        academicManagementService.deleteStandard(standardId);
    }
}
