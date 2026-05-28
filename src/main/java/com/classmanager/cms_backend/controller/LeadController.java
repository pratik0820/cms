package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.ConvertLeadToAdmissionRequest;
import com.classmanager.cms_backend.dto.request.CreateLeadFollowUpRequest;
import com.classmanager.cms_backend.dto.request.CreateLeadRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadFollowUpRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadStatusRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.LeadConversionResponse;
import com.classmanager.cms_backend.dto.response.LeadFollowUpPageResponse;
import com.classmanager.cms_backend.dto.response.LeadFollowUpResponse;
import com.classmanager.cms_backend.dto.response.LeadManagementResponse;
import com.classmanager.cms_backend.dto.response.LeadResponse;
import com.classmanager.cms_backend.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/super-admin/leads")
@RequiredArgsConstructor
@Tag(name = "Leads", description = "Lead inquiry and admission pipeline APIs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class LeadController extends BaseController {

    private final LeadService leadService;

    @GetMapping
    @Operation(summary = "Get lead summary and paginated lead list")
    public ResponseEntity<ApiResponse<LeadManagementResponse>> getLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leadSource,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID batchId,
            @RequestParam(required = false) UUID createdByUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                leadService.getLeads(search, branchId, status, leadSource, courseId, batchId, createdByUserId, page, size)));
    }

    @GetMapping("/{leadId}")
    @Operation(summary = "Get lead detail")
    public ResponseEntity<ApiResponse<LeadResponse>> getLead(@PathVariable UUID leadId) {
        return ResponseEntity.ok(ApiResponse.success(leadService.getLead(leadId)));
    }

    @PostMapping
    @Operation(summary = "Create a new lead inquiry")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody CreateLeadRequest request) {
        return ResponseEntity.ok(ApiResponse.success(leadService.createLead(request, currentUserId()), "Lead created successfully"));
    }

    @PutMapping("/{leadId}")
    @Operation(summary = "Update a lead inquiry")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(@PathVariable UUID leadId, @RequestBody UpdateLeadRequest request) {
        return ResponseEntity.ok(ApiResponse.success(leadService.updateLead(leadId, request, currentUserId()), "Lead updated successfully"));
    }

    @PatchMapping("/{leadId}/status")
    @Operation(summary = "Update lead status and add follow-up history")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLeadStatus(@PathVariable UUID leadId,
                                                                      @Valid @RequestBody UpdateLeadStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(leadService.updateLeadStatus(leadId, request, currentUserId()), "Lead status updated successfully"));
    }

    @PostMapping("/{leadId}/convert-to-admission")
    @Operation(summary = "Convert lead to admission and optionally enrol into a batch")
    public ResponseEntity<ApiResponse<LeadConversionResponse>> convertLeadToAdmission(@PathVariable UUID leadId,
                                                                                      @RequestBody ConvertLeadToAdmissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(leadService.convertToAdmission(leadId, request, currentUserId()), "Lead converted successfully"));
    }

    @DeleteMapping("/{leadId}")
    @Operation(summary = "Soft delete a lead")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable UUID leadId) {
        leadService.deleteLead(leadId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Lead deleted successfully"));
    }

    @GetMapping("/follow-ups")
    @Operation(summary = "Search and filter lead follow-ups")
    public ResponseEntity<ApiResponse<LeadFollowUpPageResponse>> getFollowUps(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String modeOfContact,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                leadService.getFollowUps(search, branchId, status, userId, modeOfContact, startDate, endDate, page, size)));
    }

    @PostMapping("/{leadId}/follow-ups")
    @Operation(summary = "Add a follow-up record for a lead")
    public ResponseEntity<ApiResponse<LeadFollowUpResponse>> createFollowUp(@PathVariable UUID leadId,
                                                                            @Valid @RequestBody CreateLeadFollowUpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(leadService.createFollowUp(leadId, request, currentUserId()), "Follow-up added successfully"));
    }

    @GetMapping("/{leadId}/follow-ups")
    @Operation(summary = "Get follow-up history for a specific lead")
    public ResponseEntity<ApiResponse<List<LeadFollowUpResponse>>> getLeadFollowUps(@PathVariable UUID leadId) {
        return ResponseEntity.ok(ApiResponse.success(leadService.getLeadFollowUps(leadId)));
    }

    @PutMapping("/follow-ups/{followUpId}")
    @Operation(summary = "Update a follow-up record")
    public ResponseEntity<ApiResponse<LeadFollowUpResponse>> updateFollowUp(@PathVariable UUID followUpId,
                                                                            @RequestBody UpdateLeadFollowUpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(leadService.updateFollowUp(followUpId, request, currentUserId()), "Follow-up updated successfully"));
    }
}
