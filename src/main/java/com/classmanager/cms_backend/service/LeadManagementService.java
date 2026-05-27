package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.ConvertLeadToAdmissionRequest;
import com.classmanager.cms_backend.dto.request.CreateEnrolmentRequest;
import com.classmanager.cms_backend.dto.request.CreateLeadRequest;
import com.classmanager.cms_backend.dto.request.CreateLeadFollowUpRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadRequest;
import com.classmanager.cms_backend.dto.request.UpdateLeadStatusRequest;
import com.classmanager.cms_backend.dto.response.LeadManagementResponse;
import com.classmanager.cms_backend.dto.response.LeadConversionResponse;
import com.classmanager.cms_backend.dto.response.LeadResponse;
import com.classmanager.cms_backend.dto.response.LeadFollowUpResponse;
import com.classmanager.cms_backend.dto.response.LeadFollowUpPageResponse;
import com.classmanager.cms_backend.dto.response.StudentEnrolmentResponse;
import com.classmanager.cms_backend.entity.*;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.FeePaymentPlan;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadManagementService {

    private static final String MODULE_LEAD = "LEAD";
    private static final List<String> SUPPORTED_STATUSES = List.of(
            "NEW", "CONTACTED", "IN_FOLLOW_UP", "CONVERTED", "NOT_INTERESTED"
    );

    private final LeadInquiryRepository leadInquiryRepository;
    private final LeadFollowUpRepository leadFollowUpRepository;
    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final OperationalRecordRepository operationalRecordRepository;
    private final StudentEnrolmentService studentEnrolmentService;
    private final SubjectResponseMapper subjectResponseMapper;

    @Transactional(readOnly = true)
    public LeadManagementResponse getLeads(String search,
                                           UUID branchId,
                                           String status,
                                           String leadSource,
                                           UUID courseId,
                                           UUID batchId,
                                           UUID createdByUserId,
                                           int page,
                                           int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<LeadInquiry> leadPage = leadInquiryRepository.searchLeads(
                buildSearchPattern(search),
                branchId,
                trimToNull(status),
                trimToNull(leadSource),
                courseId,
                batchId,
                createdByUserId,
                pageable
        );

        return LeadManagementResponse.builder()
                .summary(LeadManagementResponse.Summary.builder()
                        .totalLeads(leadInquiryRepository.countByOptionalBranchAndStatus(branchId, null, createdByUserId))
                        .newLeads(leadInquiryRepository.countByOptionalBranchAndStatus(branchId, "NEW", createdByUserId))
                        .contacted(leadInquiryRepository.countByOptionalBranchAndStatus(branchId, "CONTACTED", createdByUserId))
                        .inFollowUp(leadInquiryRepository.countByOptionalBranchAndStatus(branchId, "IN_FOLLOW_UP", createdByUserId))
                        .converted(leadInquiryRepository.countByOptionalBranchAndStatus(branchId, "CONVERTED", createdByUserId))
                        .notInterested(leadInquiryRepository.countByOptionalBranchAndStatus(branchId, "NOT_INTERESTED", createdByUserId))
                        .build())
                .content(leadPage.getContent().stream().map(lead -> toResponse(lead, false)).toList())
                .page(LeadManagementResponse.PageMeta.builder()
                        .pageNumber(leadPage.getNumber())
                        .pageSize(leadPage.getSize())
                        .totalElements(leadPage.getTotalElements())
                        .totalPages(leadPage.getTotalPages())
                        .first(leadPage.isFirst())
                        .last(leadPage.isLast())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public LeadResponse getLead(UUID leadId) {
        return toResponse(loadLead(leadId), true);
    }

    @Transactional
    public LeadResponse createLead(CreateLeadRequest request, UUID createdByUserId) {
        User creator = loadUser(createdByUserId);
        LeadInquiry lead = LeadInquiry.builder()
                .leadCode(generateLeadCode())
                .status("NEW")
                .createdByUser(creator)
                .build();
        applyCreateFields(lead, request);
        lead = leadInquiryRepository.save(lead);
        recordLeadActivity(lead, "LEAD_CREATED", "NEW", "Lead inquiry created", createdByUserId);
        return toResponse(lead, true);
    }

    @Transactional
    public LeadResponse updateLead(UUID leadId, UpdateLeadRequest request, UUID updatedByUserId) {
        LeadInquiry lead = loadLead(leadId);
        applyUpdateFields(lead, request);
        lead = leadInquiryRepository.save(lead);
        recordLeadActivity(lead, "LEAD_UPDATED", lead.getStatus(), "Lead inquiry updated", updatedByUserId);
        return toResponse(lead, true);
    }

    @Transactional
    public LeadResponse updateLeadStatus(UUID leadId, UpdateLeadStatusRequest request, UUID updatedByUserId) {
        LeadInquiry lead = loadLead(leadId);
        String status = normalizeStatus(request.getStatus());
        lead.setStatus(status);
        lead.setNextFollowUpAt(request.getNextFollowUpAt());

        LeadFollowUp followUp = LeadFollowUp.builder()
                .lead(lead)
                .followUpAt(LocalDateTime.now())
                .modeOfContact(trimToNull(request.getModeOfContact()))
                .notes(trimToNull(request.getNotes()))
                .nextFollowUpAt(request.getNextFollowUpAt())
                .statusAfter(status)
                .createdByUser(loadUser(updatedByUserId))
                .build();
        leadFollowUpRepository.save(followUp);
        lead = leadInquiryRepository.save(lead);

        recordLeadActivity(lead, "LEAD_STATUS_UPDATED", analyticsStatus(status),
                "Lead status updated to " + status, updatedByUserId);
        return toResponse(lead, true);
    }

    @Transactional
    public LeadConversionResponse convertToAdmission(UUID leadId,
                                                     ConvertLeadToAdmissionRequest request,
                                                     UUID convertedByUserId) {
        LeadInquiry lead = loadLead(leadId);
        if ("CONVERTED".equalsIgnoreCase(lead.getStatus()) && lead.getConvertedStudent() != null) {
            throw new BadRequestException("Lead is already converted", "LEAD_ALREADY_CONVERTED");
        }

        Branch branch = resolveBranch(request.getBranchId() != null
                ? request.getBranchId()
                : lead.getPreferredBranch() != null ? lead.getPreferredBranch().getId() : null);
        if (branch == null) {
            throw new BadRequestException("Branch is required to convert lead to admission", "BRANCH_REQUIRED");
        }

        Student student = createStudentFromLead(lead, request, branch);
        lead.setStatus("CONVERTED");
        lead.setConvertedStudent(student);
        lead.setConvertedAt(LocalDateTime.now());
        leadInquiryRepository.save(lead);
        recordLeadActivity(lead, "LEAD_CONVERTED", "ADMISSION_COMPLETED",
                "Lead converted to admission", convertedByUserId);

        StudentEnrolmentResponse enrolment = null;

        UUID finalBatchId = request.getBatchId() != null ? request.getBatchId() : (lead.getBatch() != null ? lead.getBatch().getId() : null);
        BigDecimal finalTotalFee = request.getAgreedTotalFee() != null ? request.getAgreedTotalFee() : 
                (lead.getFinalPayableFee() != null ? lead.getFinalPayableFee() : lead.getTotalBaseFee());
        List<UUID> finalSubjectIds = (request.getSubjectIds() != null && !request.getSubjectIds().isEmpty()) 
                ? request.getSubjectIds() 
                : (lead.getSubjects() != null ? lead.getSubjects().stream().map(Subject::getId).toList() : null);

        if (finalBatchId != null && finalTotalFee != null && finalSubjectIds != null && !finalSubjectIds.isEmpty()) {
            CreateEnrolmentRequest enrolmentRequest = new CreateEnrolmentRequest();
            enrolmentRequest.setStudentId(student.getId());
            enrolmentRequest.setBatchId(finalBatchId);
            enrolmentRequest.setSubjectGroupId(request.getSubjectGroupId());
            enrolmentRequest.setSubjectIds(finalSubjectIds);
            enrolmentRequest.setAgreedTotalFee(finalTotalFee);
            enrolmentRequest.setPaymentPlan(request.getPaymentPlan() != null ? request.getPaymentPlan() : FeePaymentPlan.REGULAR);
            enrolmentRequest.setEnrolmentDate(request.getAdmissionDate() != null ? request.getAdmissionDate() : LocalDate.now());
            
            List<CreateEnrolmentRequest.InstalmentRequest> finalInstalments = request.getInstalments();
            if ((finalInstalments == null || finalInstalments.isEmpty()) && lead.getInstallments() != null && !lead.getInstallments().isEmpty()) {
                finalInstalments = lead.getInstallments().stream().map(inst -> {
                    CreateEnrolmentRequest.InstalmentRequest ir = new CreateEnrolmentRequest.InstalmentRequest();
                    ir.setInstalmentNumber(inst.getInstalmentNumber());
                    ir.setLabel("Instalment " + inst.getInstalmentNumber());
                    ir.setAmount(inst.getAmount());
                    ir.setDueDate(inst.getDueDate());
                    return ir;
                }).toList();
            }
            enrolmentRequest.setInstalments(finalInstalments);
            
            enrolmentRequest.setNotes(request.getNotes());
            enrolment = studentEnrolmentService.createEnrolment(enrolmentRequest);
        }

        return LeadConversionResponse.builder()
                .leadId(lead.getId())
                .leadCode(lead.getLeadCode())
                .studentId(student.getId())
                .visibleStudentId(student.getStudentId())
                .convertedAt(lead.getConvertedAt())
                .enrolment(enrolment)
                .build();
    }

    @Transactional
    public void deleteLead(UUID leadId, UUID deletedByUserId) {
        LeadInquiry lead = loadLead(leadId);
        lead.softDelete();
        leadInquiryRepository.save(lead);
        recordLeadActivity(lead, "LEAD_DELETED", lead.getStatus(), "Lead inquiry soft deleted", deletedByUserId);
    }

    private Student createStudentFromLead(LeadInquiry lead, ConvertLeadToAdmissionRequest request, Branch branch) {
        String visibleStudentId = StringUtils.hasText(request.getStudentId())
                ? request.getStudentId().trim().toUpperCase(Locale.ENGLISH)
                : "STU" + System.currentTimeMillis();
        studentRepository.findByStudentIdIgnoreCaseAndIsDeletedFalse(visibleStudentId)
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("A student with this student ID already exists.");
                });

        Student student = Student.builder()
                .branch(branch)
                .name(lead.getStudentName())
                .studentId(visibleStudentId)
                .dob(lead.getDateOfBirth())
                .gender(lead.getGender())
                .mobile(lead.getMobileNumber())
                .parentName(resolveParentName(lead))
                .parentPhone(resolveParentPhone(lead))
                .email(lead.getEmail())
                .address(lead.getAddress())
                .schoolName(lead.getCurrentSchool())
                .standard(StringUtils.hasText(request.getStandard()) ? request.getStandard().trim() : lead.getClassInterestedIn())
                .batch(StringUtils.hasText(request.getBatch()) ? request.getBatch().trim() : lead.getBatchSuggested())
                .board(parseBoard(request.getBoard() != null ? request.getBoard() : lead.getBoard()))
                .admissionDate(request.getAdmissionDate() != null ? request.getAdmissionDate() : LocalDate.now())
                .isAdmissionFinal(true)
                .isActive(true)
                .build();
        return studentRepository.save(student);
    }

    private void applyCreateFields(LeadInquiry lead, CreateLeadRequest request) {
        lead.setStudentName(required(request.getStudentName(), "Student name is required"));
        lead.setMobileNumber(required(request.getMobileNumber(), "Mobile number is required"));
        lead.setLeadSource(required(request.getLeadSource(), "Lead source is required"));
        lead.setPreferredBranch(resolveBranch(request.getPreferredBranchId()));
        lead.setInquiryFor(trimToNull(request.getInquiryFor()));
        lead.setCourse(resolveCourse(request.getCourseId()));
        lead.setBatch(resolveBatch(request.getBatchId()));
        lead.setSubjects(resolveSubjects(request.getSubjectIds()));
        lead.setRecommendedSubjects(resolveSubjects(request.getRecommendedSubjectIds()));
        applySharedFields(lead, request);
        lead.setAssignedToUser(resolveUser(request.getAssignedToUserId()));
        lead.setCounsellorUser(resolveUser(request.getCounsellorUserId()));
    }

    private void applyUpdateFields(LeadInquiry lead, UpdateLeadRequest request) {
        if (request.getStudentName() != null) lead.setStudentName(required(request.getStudentName(), "Student name is required"));
        if (request.getMobileNumber() != null) lead.setMobileNumber(required(request.getMobileNumber(), "Mobile number is required"));
        if (request.getLeadSource() != null) lead.setLeadSource(required(request.getLeadSource(), "Lead source is required"));
        if (request.getPreferredBranchId() != null) lead.setPreferredBranch(resolveBranch(request.getPreferredBranchId()));
        if (request.getInquiryFor() != null) lead.setInquiryFor(trimToNull(request.getInquiryFor()));
        if (request.getCourseId() != null) lead.setCourse(resolveCourse(request.getCourseId()));
        if (request.getBatchId() != null) lead.setBatch(resolveBatch(request.getBatchId()));
        if (request.getSubjectIds() != null) lead.setSubjects(resolveSubjects(request.getSubjectIds()));
        if (request.getRecommendedSubjectIds() != null) lead.setRecommendedSubjects(resolveSubjects(request.getRecommendedSubjectIds()));
        applySharedFields(lead, request);
        if (request.getAssignedToUserId() != null) lead.setAssignedToUser(resolveUser(request.getAssignedToUserId()));
        if (request.getCounsellorUserId() != null) lead.setCounsellorUser(resolveUser(request.getCounsellorUserId()));
    }

    private void applySharedFields(LeadInquiry lead, Object request) {
        if (request instanceof CreateLeadRequest r) {
            lead.setGender(trimToNull(r.getGender()));
            lead.setDateOfBirth(r.getDateOfBirth());
            lead.setBloodGroup(trimToNull(r.getBloodGroup()));
            lead.setClassInterestedIn(trimToNull(r.getClassInterestedIn()));
            lead.setBoard(trimToNull(r.getBoard()));
            lead.setMedium(trimToNull(r.getMedium()));
            lead.setStream(trimToNull(r.getStream()));
            lead.setCurrentSchool(trimToNull(r.getCurrentSchool()));
            lead.setLastClassCompleted(trimToNull(r.getLastClassCompleted()));
            lead.setLastExamPercentage(trimToNull(r.getLastExamPercentage()));
            lead.setAddress(trimToNull(r.getAddress()));
            lead.setMobileCountryCode(trimToNull(r.getMobileCountryCode()));
            lead.setAlternateMobileCountryCode(trimToNull(r.getAlternateMobileCountryCode()));
            lead.setAlternateMobileNumber(trimToNull(r.getAlternateMobileNumber()));
            lead.setEmail(normalizeEmail(r.getEmail()));
            lead.setFatherName(trimToNull(r.getFatherName()));
            lead.setMotherName(trimToNull(r.getMotherName()));
            lead.setGuardianName(trimToNull(r.getGuardianName()));
            lead.setRelation(trimToNull(r.getRelation()));
            lead.setFatherMobileCountryCode(trimToNull(r.getFatherMobileCountryCode()));
            lead.setFatherMobileNumber(trimToNull(r.getFatherMobileNumber()));
            lead.setMotherMobileCountryCode(trimToNull(r.getMotherMobileCountryCode()));
            lead.setMotherMobileNumber(trimToNull(r.getMotherMobileNumber()));
            lead.setGuardianMobileCountryCode(trimToNull(r.getGuardianMobileCountryCode()));
            lead.setGuardianMobileNumber(trimToNull(r.getGuardianMobileNumber()));
            lead.setParentEmail(normalizeEmail(r.getParentEmail()));
            lead.setFatherOccupation(trimToNull(r.getFatherOccupation()));
            lead.setMotherOccupation(trimToNull(r.getMotherOccupation()));
            lead.setAnnualIncome(trimToNull(r.getAnnualIncome()));
            lead.setNationality(trimToNull(r.getNationality()));
            lead.setReferredBy(trimToNull(r.getReferredBy()));
            lead.setHeardAboutUs(trimToNull(r.getHeardAboutUs()));
            lead.setExpectedAdmissionYear(trimToNull(r.getExpectedAdmissionYear()));
            lead.setPreferredAdmissionDate(r.getPreferredAdmissionDate());
            lead.setPreferredContactTime(trimToNull(r.getPreferredContactTime()));
            lead.setModeOfContact(trimToNull(r.getModeOfContact()));
            lead.setBestDaysToContact(trimToNull(r.getBestDaysToContact()));
            lead.setCourseRecommended(trimToNull(r.getCourseRecommended()));
            lead.setSubjectsSuggested(trimToNull(r.getSubjectsSuggested()));
            lead.setBatchSuggested(trimToNull(r.getBatchSuggested()));
            lead.setAdmissionLikelihood(trimToNull(r.getAdmissionLikelihood()));
            lead.setRemarks(trimToNull(r.getRemarks()));
            lead.setNextFollowUpAt(r.getNextFollowUpAt());
            
            // Fee & Payment Structure
            lead.setTotalBaseFee(r.getTotalBaseFee());
            lead.setMeritScholarship(trimToNull(r.getMeritScholarship()));
            lead.setAdditionalConcessionAmount(r.getAdditionalConcessionAmount());
            lead.setAdditionalCategoryName(trimToNull(r.getAdditionalCategoryName()));
            lead.setAdditionalCategoryDiscountAmount(r.getAdditionalCategoryDiscountAmount());
            lead.setFinalPayableFee(r.getFinalPayableFee());
            lead.setTokenAmountPaid(r.getTokenAmountPaid());
            lead.setModeOfPayment(trimToNull(r.getModeOfPayment()));
            lead.setPaymentStructure(trimToNull(r.getPaymentStructure()));
            lead.setFinancialAssistanceRequired(r.getFinancialAssistanceRequired());
            lead.setExternalScholarshipApplicable(r.getExternalScholarshipApplicable());
            lead.setExternalScholarshipDetails(trimToNull(r.getExternalScholarshipDetails()));
            lead.setTokenPaymentMode(trimToNull(r.getTokenPaymentMode()));
            lead.setTokenRemarks(trimToNull(r.getTokenRemarks()));

            if (r.getInstallments() != null) {
                lead.getInstallments().clear();
                r.getInstallments().forEach(inst -> {
                    LeadInquiryInstalment instalment = LeadInquiryInstalment.builder()
                            .lead(lead)
                            .instalmentNumber(inst.getInstalmentNumber())
                            .amount(inst.getAmount())
                            .dueDate(inst.getDueDate())
                            .build();
                    lead.getInstallments().add(instalment);
                });
            }
        } else if (request instanceof UpdateLeadRequest r) {
            if (r.getGender() != null) lead.setGender(trimToNull(r.getGender()));
            if (r.getDateOfBirth() != null) lead.setDateOfBirth(r.getDateOfBirth());
            if (r.getBloodGroup() != null) lead.setBloodGroup(trimToNull(r.getBloodGroup()));
            if (r.getClassInterestedIn() != null) lead.setClassInterestedIn(trimToNull(r.getClassInterestedIn()));
            if (r.getBoard() != null) lead.setBoard(trimToNull(r.getBoard()));
            if (r.getMedium() != null) lead.setMedium(trimToNull(r.getMedium()));
            if (r.getStream() != null) lead.setStream(trimToNull(r.getStream()));
            if (r.getCurrentSchool() != null) lead.setCurrentSchool(trimToNull(r.getCurrentSchool()));
            if (r.getLastClassCompleted() != null) lead.setLastClassCompleted(trimToNull(r.getLastClassCompleted()));
            if (r.getLastExamPercentage() != null) lead.setLastExamPercentage(trimToNull(r.getLastExamPercentage()));
            if (r.getAddress() != null) lead.setAddress(trimToNull(r.getAddress()));
            if (r.getMobileCountryCode() != null) lead.setMobileCountryCode(trimToNull(r.getMobileCountryCode()));
            if (r.getAlternateMobileCountryCode() != null) lead.setAlternateMobileCountryCode(trimToNull(r.getAlternateMobileCountryCode()));
            if (r.getAlternateMobileNumber() != null) lead.setAlternateMobileNumber(trimToNull(r.getAlternateMobileNumber()));
            if (r.getEmail() != null) lead.setEmail(normalizeEmail(r.getEmail()));
            if (r.getFatherName() != null) lead.setFatherName(trimToNull(r.getFatherName()));
            if (r.getMotherName() != null) lead.setMotherName(trimToNull(r.getMotherName()));
            if (r.getGuardianName() != null) lead.setGuardianName(trimToNull(r.getGuardianName()));
            if (r.getRelation() != null) lead.setRelation(trimToNull(r.getRelation()));
            if (r.getFatherMobileCountryCode() != null) lead.setFatherMobileCountryCode(trimToNull(r.getFatherMobileCountryCode()));
            if (r.getFatherMobileNumber() != null) lead.setFatherMobileNumber(trimToNull(r.getFatherMobileNumber()));
            if (r.getMotherMobileCountryCode() != null) lead.setMotherMobileCountryCode(trimToNull(r.getMotherMobileCountryCode()));
            if (r.getMotherMobileNumber() != null) lead.setMotherMobileNumber(trimToNull(r.getMotherMobileNumber()));
            if (r.getGuardianMobileCountryCode() != null) lead.setGuardianMobileCountryCode(trimToNull(r.getGuardianMobileCountryCode()));
            if (r.getGuardianMobileNumber() != null) lead.setGuardianMobileNumber(trimToNull(r.getGuardianMobileNumber()));
            if (r.getParentEmail() != null) lead.setParentEmail(normalizeEmail(r.getParentEmail()));
            if (r.getFatherOccupation() != null) lead.setFatherOccupation(trimToNull(r.getFatherOccupation()));
            if (r.getMotherOccupation() != null) lead.setMotherOccupation(trimToNull(r.getMotherOccupation()));
            if (r.getAnnualIncome() != null) lead.setAnnualIncome(trimToNull(r.getAnnualIncome()));
            if (r.getNationality() != null) lead.setNationality(trimToNull(r.getNationality()));
            if (r.getReferredBy() != null) lead.setReferredBy(trimToNull(r.getReferredBy()));
            if (r.getHeardAboutUs() != null) lead.setHeardAboutUs(trimToNull(r.getHeardAboutUs()));
            if (r.getExpectedAdmissionYear() != null) lead.setExpectedAdmissionYear(trimToNull(r.getExpectedAdmissionYear()));
            if (r.getPreferredAdmissionDate() != null) lead.setPreferredAdmissionDate(r.getPreferredAdmissionDate());
            if (r.getPreferredContactTime() != null) lead.setPreferredContactTime(trimToNull(r.getPreferredContactTime()));
            if (r.getModeOfContact() != null) lead.setModeOfContact(trimToNull(r.getModeOfContact()));
            if (r.getBestDaysToContact() != null) lead.setBestDaysToContact(trimToNull(r.getBestDaysToContact()));
            if (r.getCourseRecommended() != null) lead.setCourseRecommended(trimToNull(r.getCourseRecommended()));
            if (r.getSubjectsSuggested() != null) lead.setSubjectsSuggested(trimToNull(r.getSubjectsSuggested()));
            if (r.getBatchSuggested() != null) lead.setBatchSuggested(trimToNull(r.getBatchSuggested()));
            if (r.getAdmissionLikelihood() != null) lead.setAdmissionLikelihood(trimToNull(r.getAdmissionLikelihood()));
            if (r.getRemarks() != null) lead.setRemarks(trimToNull(r.getRemarks()));
            if (r.getNextFollowUpAt() != null) lead.setNextFollowUpAt(r.getNextFollowUpAt());

            // Fee & Payment Structure
            if (r.getTotalBaseFee() != null) lead.setTotalBaseFee(r.getTotalBaseFee());
            if (r.getMeritScholarship() != null) lead.setMeritScholarship(trimToNull(r.getMeritScholarship()));
            if (r.getAdditionalConcessionAmount() != null) lead.setAdditionalConcessionAmount(r.getAdditionalConcessionAmount());
            if (r.getAdditionalCategoryName() != null) lead.setAdditionalCategoryName(trimToNull(r.getAdditionalCategoryName()));
            if (r.getAdditionalCategoryDiscountAmount() != null) lead.setAdditionalCategoryDiscountAmount(r.getAdditionalCategoryDiscountAmount());
            if (r.getFinalPayableFee() != null) lead.setFinalPayableFee(r.getFinalPayableFee());
            if (r.getTokenAmountPaid() != null) lead.setTokenAmountPaid(r.getTokenAmountPaid());
            if (r.getModeOfPayment() != null) lead.setModeOfPayment(trimToNull(r.getModeOfPayment()));
            if (r.getPaymentStructure() != null) lead.setPaymentStructure(trimToNull(r.getPaymentStructure()));
            if (r.getFinancialAssistanceRequired() != null) lead.setFinancialAssistanceRequired(r.getFinancialAssistanceRequired());
            if (r.getExternalScholarshipApplicable() != null) lead.setExternalScholarshipApplicable(r.getExternalScholarshipApplicable());
            if (r.getExternalScholarshipDetails() != null) lead.setExternalScholarshipDetails(trimToNull(r.getExternalScholarshipDetails()));
            if (r.getTokenPaymentMode() != null) lead.setTokenPaymentMode(trimToNull(r.getTokenPaymentMode()));
            if (r.getTokenRemarks() != null) lead.setTokenRemarks(trimToNull(r.getTokenRemarks()));

            if (r.getInstallments() != null) {
                lead.getInstallments().clear();
                r.getInstallments().forEach(inst -> {
                    LeadInquiryInstalment instalment = LeadInquiryInstalment.builder()
                            .lead(lead)
                            .instalmentNumber(inst.getInstalmentNumber())
                            .amount(inst.getAmount())
                            .dueDate(inst.getDueDate())
                            .build();
                    lead.getInstallments().add(instalment);
                });
            }
        }
    }

    private LeadResponse toResponse(LeadInquiry lead, boolean includeFollowUps) {
        return LeadResponse.builder()
                .id(lead.getId())
                .leadCode(lead.getLeadCode())
                .studentName(lead.getStudentName())
                .gender(lead.getGender())
                .dateOfBirth(lead.getDateOfBirth())
                .bloodGroup(lead.getBloodGroup())
                .classInterestedIn(lead.getClassInterestedIn())
                .board(lead.getBoard())
                .medium(lead.getMedium())
                .stream(lead.getStream())
                .currentSchool(lead.getCurrentSchool())
                .lastClassCompleted(lead.getLastClassCompleted())
                .lastExamPercentage(lead.getLastExamPercentage())
                .address(lead.getAddress())
                .mobileCountryCode(lead.getMobileCountryCode())
                .mobileNumber(lead.getMobileNumber())
                .alternateMobileCountryCode(lead.getAlternateMobileCountryCode())
                .alternateMobileNumber(lead.getAlternateMobileNumber())
                .email(lead.getEmail())
                .fatherName(lead.getFatherName())
                .motherName(lead.getMotherName())
                .guardianName(lead.getGuardianName())
                .relation(lead.getRelation())
                .fatherMobileCountryCode(lead.getFatherMobileCountryCode())
                .fatherMobileNumber(lead.getFatherMobileNumber())
                .motherMobileCountryCode(lead.getMotherMobileCountryCode())
                .motherMobileNumber(lead.getMotherMobileNumber())
                .guardianMobileCountryCode(lead.getGuardianMobileCountryCode())
                .guardianMobileNumber(lead.getGuardianMobileNumber())
                .parentEmail(lead.getParentEmail())
                .fatherOccupation(lead.getFatherOccupation())
                .motherOccupation(lead.getMotherOccupation())
                .annualIncome(lead.getAnnualIncome())
                .nationality(lead.getNationality())
                .leadSource(lead.getLeadSource())
                .referredBy(lead.getReferredBy())
                .heardAboutUs(lead.getHeardAboutUs())
                .preferredBranchId(lead.getPreferredBranch() != null ? lead.getPreferredBranch().getId() : null)
                .preferredBranchName(lead.getPreferredBranch() != null ? lead.getPreferredBranch().getName() : null)
                .inquiryFor(lead.getInquiryFor())
                .courseId(lead.getCourse() != null ? lead.getCourse().getId() : null)
                .courseName(lead.getCourse() != null ? lead.getCourse().getName() : null)
                .batchId(lead.getBatch() != null ? lead.getBatch().getId() : null)
                .batchName(lead.getBatch() != null ? lead.getBatch().getName() : null)
                .subjects(lead.getSubjects().stream().map(subjectResponseMapper::toSubjectResponse).toList())
                .expectedAdmissionYear(lead.getExpectedAdmissionYear())
                .preferredAdmissionDate(lead.getPreferredAdmissionDate())
                .preferredContactTime(lead.getPreferredContactTime())
                .modeOfContact(lead.getModeOfContact())
                .bestDaysToContact(lead.getBestDaysToContact())
                .courseRecommended(lead.getCourseRecommended())
                .recommendedSubjects(lead.getRecommendedSubjects().stream().map(subjectResponseMapper::toSubjectResponse).toList())
                .subjectsSuggested(lead.getSubjectsSuggested())
                .batchSuggested(lead.getBatchSuggested())
                .admissionLikelihood(lead.getAdmissionLikelihood())
                .remarks(lead.getRemarks())
                .nextFollowUpAt(lead.getNextFollowUpAt())
                .status(lead.getStatus())
                .counsellorUserId(lead.getCounsellorUser() != null ? lead.getCounsellorUser().getId() : null)
                .counsellorName(lead.getCounsellorUser() != null ? lead.getCounsellorUser().getFullName() : null)
                .assignedToUserId(lead.getAssignedToUser() != null ? lead.getAssignedToUser().getId() : null)
                .assignedToName(lead.getAssignedToUser() != null ? lead.getAssignedToUser().getFullName() : null)
                .createdByUserId(lead.getCreatedByUser() != null ? lead.getCreatedByUser().getId() : null)
                .createdByName(lead.getCreatedByUser() != null ? lead.getCreatedByUser().getFullName() : null)
                .convertedStudentId(lead.getConvertedStudent() != null ? lead.getConvertedStudent().getId() : null)
                .convertedAt(lead.getConvertedAt())
                .followUps(includeFollowUps ? buildFollowUps(lead) : null)
                
                // Fee & Payment Structure
                .totalBaseFee(lead.getTotalBaseFee())
                .meritScholarship(lead.getMeritScholarship())
                .additionalConcessionAmount(lead.getAdditionalConcessionAmount())
                .additionalCategoryName(lead.getAdditionalCategoryName())
                .additionalCategoryDiscountAmount(lead.getAdditionalCategoryDiscountAmount())
                .finalPayableFee(lead.getFinalPayableFee())
                .tokenAmountPaid(lead.getTokenAmountPaid())
                .modeOfPayment(lead.getModeOfPayment())
                .paymentStructure(lead.getPaymentStructure())
                .financialAssistanceRequired(lead.getFinancialAssistanceRequired())
                .externalScholarshipApplicable(lead.getExternalScholarshipApplicable())
                .externalScholarshipDetails(lead.getExternalScholarshipDetails())
                .tokenPaymentMode(lead.getTokenPaymentMode())
                .tokenRemarks(lead.getTokenRemarks())
                .installments(lead.getInstallments().stream().map(inst -> LeadResponse.InstalmentResponse.builder()
                        .id(inst.getId())
                        .instalmentNumber(inst.getInstalmentNumber())
                        .amount(inst.getAmount())
                        .dueDate(inst.getDueDate())
                        .build()).toList())
                        
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }

    private List<LeadResponse.FollowUpResponse> buildFollowUps(LeadInquiry lead) {
        return leadFollowUpRepository.findByLead_IdAndIsDeletedFalseOrderByFollowUpAtDesc(lead.getId()).stream()
                .map(item -> LeadResponse.FollowUpResponse.builder()
                        .id(item.getId())
                        .followUpAt(item.getFollowUpAt())
                        .modeOfContact(item.getModeOfContact())
                        .notes(item.getNotes())
                        .nextFollowUpAt(item.getNextFollowUpAt())
                        .statusAfter(item.getStatusAfter())
                        .createdByName(item.getCreatedByUser() != null ? item.getCreatedByUser().getFullName() : null)
                        .spokeWith(item.getSpokeWith())
                        .remarks(item.getRemarks())
                        .nextFollowUpType(item.getNextFollowUpType())
                        .nextFollowUpMode(item.getNextFollowUpMode())
                        .nextFollowUpByUserId(item.getNextFollowUpByUser() != null ? item.getNextFollowUpByUser().getId() : null)
                        .nextFollowUpByName(item.getNextFollowUpByUser() != null ? item.getNextFollowUpByUser().getFullName() : null)
                        .reminder(item.getReminder())
                        .priority(item.getPriority())
                        .nextFollowUpNotes(item.getNextFollowUpNotes())
                        .build())
                .toList();
    }

    private LeadInquiry loadLead(UUID leadId) {
        return leadInquiryRepository.findByIdAndIsDeletedFalse(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", leadId));
    }

    private User loadUser(UUID userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private User resolveUser(UUID userId) {
        return userId == null ? null : loadUser(userId);
    }

    private Branch resolveBranch(UUID branchId) {
        if (branchId == null) return null;
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private Course resolveCourse(UUID courseId) {
        if (courseId == null) return null;
        return courseRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }

    private Batch resolveBatch(UUID batchId) {
        if (batchId == null) return null;
        return batchRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));
    }

    private List<Subject> resolveSubjects(List<UUID> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return List.of();
        }
        return subjectIds.stream()
                .map(id -> subjectRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Subject", id)))
                .toList();
    }

    private BoardType parseBoard(String board) {
        if (!StringUtils.hasText(board)) return null;
        try {
            return BoardType.valueOf(board.trim().toUpperCase(Locale.ENGLISH));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String normalizeStatus(String status) {
        String normalized = required(status, "Status is required").toUpperCase(Locale.ENGLISH).replace("-", "_").replace(" ", "_");
        if (!SUPPORTED_STATUSES.contains(normalized)) {
            throw new BadRequestException("Unsupported lead status", "INVALID_LEAD_STATUS");
        }
        return normalized;
    }

    private String analyticsStatus(String status) {
        if ("CONVERTED".equals(status)) return "CONVERTED";
        if ("NOT_INTERESTED".equals(status)) return "NOT_INTERESTED";
        if ("CONTACTED".equals(status) || "IN_FOLLOW_UP".equals(status)) return "INTERESTED";
        return status;
    }

    private String generateLeadCode() {
        String code;
        do {
            code = "INQ/" + LocalDate.now().getYear() + "/" + String.format("%06d", System.currentTimeMillis() % 1_000_000);
        } while (leadInquiryRepository.existsByLeadCodeAndIsDeletedFalse(code));
        return code;
    }

    private void recordLeadActivity(LeadInquiry lead, String type, String status, String description, UUID actorUserId) {
        operationalRecordRepository.save(OperationalRecord.builder()
                .module(MODULE_LEAD)
                .type(type)
                .title(lead.getStudentName())
                .description(description)
                .status(status)
                .branch(lead.getPreferredBranch())
                .createdByUserId(actorUserId)
                .eventDate(LocalDate.now())
                .build());
    }

    private String resolveParentName(LeadInquiry lead) {
        if (StringUtils.hasText(lead.getFatherName())) return lead.getFatherName();
        if (StringUtils.hasText(lead.getMotherName())) return lead.getMotherName();
        return lead.getGuardianName();
    }

    private String resolveParentPhone(LeadInquiry lead) {
        if (StringUtils.hasText(lead.getFatherMobileNumber())) return lead.getFatherMobileNumber();
        if (StringUtils.hasText(lead.getMotherMobileNumber())) return lead.getMotherMobileNumber();
        if (StringUtils.hasText(lead.getGuardianMobileNumber())) return lead.getGuardianMobileNumber();
        return lead.getMobileNumber();
    }

    private String buildSearchPattern(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : "%" + normalized + "%";
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ENGLISH) : null;
    }

    private String normalizeEmail(String value) {
        return normalize(value);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message, "VALIDATION_ERROR");
        }
        return value.trim();
    }

    @Transactional(readOnly = true)
    public LeadFollowUpPageResponse getFollowUps(
            String search,
            UUID branchId,
            String status,
            UUID userId,
            String modeOfContact,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size) {

        String searchPattern = buildSearchPattern(search);
        String trimmedStatus = trimToNull(status);
        if (trimmedStatus != null) {
            trimmedStatus = trimmedStatus.toUpperCase(Locale.ENGLISH);
        }
        String trimmedMode = trimToNull(modeOfContact);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "followUpAt"));
        Page<LeadFollowUp> followUpPage = leadFollowUpRepository.searchFollowUps(
                branchId, searchPattern, userId, trimmedMode, startDate, endDate, trimmedStatus, now, pageable
        );

        long total = leadFollowUpRepository.countFollowUps(branchId, searchPattern, userId, trimmedMode, startDate, endDate, null);
        long completed = leadFollowUpRepository.countFollowUps(branchId, searchPattern, userId, trimmedMode, startDate, endDate, "COMPLETED");
        long pending = leadFollowUpRepository.countFollowUps(branchId, searchPattern, userId, trimmedMode, startDate, endDate, "PENDING");
        long converted = leadFollowUpRepository.countConvertedLeads(branchId, searchPattern, userId, trimmedMode, startDate, endDate);

        double conversionPercent = total == 0 ? 0.0 : ((double) converted / total) * 100.0;
        String conversionRate = String.format(Locale.ENGLISH, "%.2f%%", conversionPercent);

        List<LeadFollowUpResponse> content = followUpPage.getContent().stream()
                .map(this::toFollowUpResponse)
                .toList();

        return LeadFollowUpPageResponse.builder()
                .summary(LeadFollowUpPageResponse.Summary.builder()
                        .totalFollowUps(total)
                        .completed(completed)
                        .pending(pending)
                        .conversionRate(conversionRate)
                        .build())
                .content(content)
                .page(LeadFollowUpPageResponse.PageMeta.builder()
                        .pageNumber(followUpPage.getNumber())
                        .pageSize(followUpPage.getSize())
                        .totalElements(followUpPage.getTotalElements())
                        .totalPages(followUpPage.getTotalPages())
                        .first(followUpPage.isFirst())
                        .last(followUpPage.isLast())
                        .build())
                .build();
    }

    @Transactional
    public LeadFollowUpResponse createFollowUp(UUID leadId, CreateLeadFollowUpRequest request, UUID actorUserId) {
        LeadInquiry lead = loadLead(leadId);
        User actor = loadUser(actorUserId);

        String outcome = request.getOutcomeStatus().toUpperCase(Locale.ENGLISH);
        if ("COMPLETED".equals(outcome)) {
            if ("NEW".equals(lead.getStatus())) {
                lead.setStatus("CONTACTED");
            }
        } else if ("PENDING".equals(outcome)) {
            lead.setStatus("IN_FOLLOW_UP");
        }

        lead.setNextFollowUpAt(request.getNextFollowUpAt());
        leadInquiryRepository.save(lead);

        User nextFollowUpBy = resolveUser(request.getNextFollowUpByUserId());

        LeadFollowUp followUp = LeadFollowUp.builder()
                .lead(lead)
                .followUpAt(request.getFollowUpAt())
                .modeOfContact(request.getFollowUpMode())
                .spokeWith(request.getSpokeWith())
                .notes(request.getNotes())
                .statusAfter(request.getOutcomeStatus())
                .remarks(request.getRemarks())
                .nextFollowUpAt(request.getNextFollowUpAt())
                .nextFollowUpType(request.getNextFollowUpType())
                .nextFollowUpMode(request.getNextFollowUpMode())
                .nextFollowUpByUser(nextFollowUpBy)
                .reminder(request.getReminder())
                .priority(request.getPriority())
                .nextFollowUpNotes(request.getNextFollowUpNotes())
                .createdByUser(actor)
                .build();

        followUp = leadFollowUpRepository.save(followUp);

        recordLeadActivity(lead, "LEAD_FOLLOW_UP_ADDED", lead.getStatus(),
                "Follow-up added by " + actor.getFullName() + ". Status: " + request.getOutcomeStatus(), actorUserId);

        return toFollowUpResponse(followUp);
    }

    @Transactional(readOnly = true)
    public List<LeadFollowUpResponse> getLeadFollowUps(UUID leadId) {
        LeadInquiry lead = loadLead(leadId);
        return leadFollowUpRepository.findByLead_IdAndIsDeletedFalseOrderByFollowUpAtDesc(lead.getId()).stream()
                .map(this::toFollowUpResponse)
                .toList();
    }

    private LeadFollowUpResponse toFollowUpResponse(LeadFollowUp item) {
        return LeadFollowUpResponse.builder()
                .id(item.getId())
                .leadId(item.getLead().getId())
                .leadCode(item.getLead().getLeadCode())
                .studentName(item.getLead().getStudentName())
                .followUpAt(item.getFollowUpAt())
                .followUpType(item.getModeOfContact())
                .followUpMode(item.getModeOfContact())
                .spokeWith(item.getSpokeWith())
                .notes(item.getNotes())
                .outcomeStatus(item.getStatusAfter())
                .remarks(item.getRemarks())
                .nextFollowUpAt(item.getNextFollowUpAt())
                .nextFollowUpType(item.getNextFollowUpType())
                .nextFollowUpMode(item.getNextFollowUpMode())
                .nextFollowUpByUserId(item.getNextFollowUpByUser() != null ? item.getNextFollowUpByUser().getId() : null)
                .nextFollowUpByName(item.getNextFollowUpByUser() != null ? item.getNextFollowUpByUser().getFullName() : null)
                .reminder(item.getReminder())
                .priority(item.getPriority())
                .nextFollowUpNotes(item.getNextFollowUpNotes())
                .followUpByName(item.getCreatedByUser() != null ? item.getCreatedByUser().getFullName() : null)
                .createdAt(item.getCreatedAt())
                .build();
    }

    public java.util.List<java.util.Map<String, Object>> getDebugLeads() {
        return leadInquiryRepository.findAll().stream().map(l -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", l.getId());
            map.put("studentName", l.getStudentName());
            map.put("branchId", l.getPreferredBranch() != null ? l.getPreferredBranch().getId() : null);
            map.put("branchName", l.getPreferredBranch() != null ? l.getPreferredBranch().getName() : null);
            map.put("createdByUserId", l.getCreatedByUser() != null ? l.getCreatedByUser().getId() : null);
            map.put("createdByName", l.getCreatedByUser() != null ? l.getCreatedByUser().getFullName() : null);
            return map;
        }).toList();
    }
}
