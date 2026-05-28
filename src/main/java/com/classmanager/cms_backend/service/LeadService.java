package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.ConvertLeadToAdmissionRequest;
import com.classmanager.cms_backend.dto.request.CreateLeadRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadStatusRequest;
import com.classmanager.cms_backend.dto.response.LeadConversionResponse;
import com.classmanager.cms_backend.dto.response.LeadManagementResponse;
import com.classmanager.cms_backend.dto.response.LeadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadManagementService leadManagementService;

    public LeadManagementResponse getLeads(String search, UUID branchId, String status, String leadSource,
                                           UUID courseId, UUID batchId, int page, int size) {
        return leadManagementService.getLeads(search, branchId, status, leadSource, courseId, batchId, page, size);
    }

    public LeadResponse getLead(UUID leadId) {
        return leadManagementService.getLead(leadId);
    }

    public LeadResponse createLead(CreateLeadRequest request, UUID createdByUserId) {
        return leadManagementService.createLead(request, createdByUserId);
    }

    public LeadResponse updateLead(UUID leadId, UpdateLeadRequest request, UUID updatedByUserId) {
        return leadManagementService.updateLead(leadId, request, updatedByUserId);
    }

    public LeadResponse updateLeadStatus(UUID leadId, UpdateLeadStatusRequest request, UUID updatedByUserId) {
        return leadManagementService.updateLeadStatus(leadId, request, updatedByUserId);
    }

    public LeadConversionResponse convertToAdmission(UUID leadId, ConvertLeadToAdmissionRequest request, UUID convertedByUserId) {
        return leadManagementService.convertToAdmission(leadId, request, convertedByUserId);
    }

    public void deleteLead(UUID leadId, UUID deletedByUserId) {
        leadManagementService.deleteLead(leadId, deletedByUserId);
    }
}
