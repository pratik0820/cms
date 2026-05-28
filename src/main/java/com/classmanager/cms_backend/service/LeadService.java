package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.ConvertLeadToAdmissionRequest;
import com.classmanager.cms_backend.dto.request.CreateLeadFollowUpRequest;
import com.classmanager.cms_backend.dto.request.CreateLeadRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadFollowUpRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadStatusRequest;
import com.classmanager.cms_backend.dto.response.LeadConversionResponse;
import com.classmanager.cms_backend.dto.response.LeadFollowUpPageResponse;
import com.classmanager.cms_backend.dto.response.LeadFollowUpResponse;
import com.classmanager.cms_backend.dto.response.LeadManagementResponse;
import com.classmanager.cms_backend.dto.response.LeadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadManagementService leadManagementService;

    public LeadManagementResponse getLeads(String search, UUID branchId, String status, String leadSource,
                                           UUID courseId, UUID batchId, UUID createdByUserId, int page, int size) {
        return leadManagementService.getLeads(search, branchId, status, leadSource, courseId, batchId, createdByUserId, page, size);
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

    public LeadFollowUpPageResponse getFollowUps(String search, UUID branchId, String status, UUID userId,
                                                 String modeOfContact, LocalDateTime startDate, LocalDateTime endDate,
                                                 int page, int size) {
        return leadManagementService.getFollowUps(search, branchId, status, userId, modeOfContact, startDate, endDate, page, size);
    }

    public LeadFollowUpResponse createFollowUp(UUID leadId, CreateLeadFollowUpRequest request, UUID actorUserId) {
        return leadManagementService.createFollowUp(leadId, request, actorUserId);
    }

    public List<LeadFollowUpResponse> getLeadFollowUps(UUID leadId) {
        return leadManagementService.getLeadFollowUps(leadId);
    }

    public LeadFollowUpResponse updateFollowUp(UUID followUpId, UpdateLeadFollowUpRequest request, UUID actorUserId) {
        return leadManagementService.updateFollowUp(followUpId, request, actorUserId);
    }
}
