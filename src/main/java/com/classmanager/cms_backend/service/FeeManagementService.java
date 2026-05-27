package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CollectFeeRequest;
import com.classmanager.cms_backend.dto.response.FeeAnalyticsResponse;
import com.classmanager.cms_backend.dto.response.FeeComponentResponse;
import com.classmanager.cms_backend.dto.response.FeeSummaryResponse;
import com.classmanager.cms_backend.dto.response.FeeTransactionResponse;
import com.classmanager.cms_backend.dto.response.StudentFeeSummaryResponse;
import com.classmanager.cms_backend.entity.*;
import com.classmanager.cms_backend.exception.BusinessRuleException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.FeeTransactionDetailRepository;
import com.classmanager.cms_backend.repository.FeeTransactionRepository;
import com.classmanager.cms_backend.repository.StudentEnrolmentRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeManagementService {

    private final StudentEnrolmentRepository enrolmentRepository;
    private final FeeTransactionRepository feeTransactionRepository;
    private final FeeTransactionDetailRepository feeTransactionDetailRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<FeeComponentResponse> getFeeComponents(UUID branchId, String month, UUID classId, String feeType, String status, String search, Pageable pageable) {
        // Since JPA queries with multiple dynamic joins for components can be complex,
        // and standard pagination on nested lists is tricky without a custom native query or QueryDSL,
        // we'll fetch enrolments and map their components.
        // For production scale, a native query or a dedicated view is recommended.
        // As a simplified approach for now, we'll fetch active enrolments and flat-map their instalments.
        
        // Fetch all active enrolments (optionally filtered by branch/search)
        // Here we just fetch all and filter in memory for simplicity (needs optimization for large scale)
        List<StudentEnrolment> enrolments = enrolmentRepository.findAll().stream()
                .filter(e -> !e.isDeleted())
                .filter(e -> branchId == null || e.getBatch().getBranch().getId().equals(branchId))
                .filter(e -> search == null || e.getStudent().getName().toLowerCase().contains(search.toLowerCase()))
                .toList();

        List<FeeComponentResponse> components = new ArrayList<>();
        for (StudentEnrolment e : enrolments) {
            for (EnrolmentInstalment inst : e.getInstalments()) {
                if (inst.isDeleted()) continue;
                
                BigDecimal amount = inst.getAmount();
                BigDecimal paid = inst.getPaidAmount() != null ? inst.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal balance = amount.subtract(paid);
                
                String compStatus = determineStatus(amount, paid, inst.getDueDate());

                // Apply filters
                if (feeType != null && !feeType.equalsIgnoreCase("All Fee Types") && !inst.getLabel().equalsIgnoreCase(feeType)) continue;
                if (status != null && !status.equalsIgnoreCase("All Status") && !compStatus.equalsIgnoreCase(status)) continue;

                components.add(FeeComponentResponse.builder()
                        .id(inst.getId())
                        .enrolmentId(e.getId())
                        .studentId(e.getStudent().getId())
                        .studentName(e.getStudent().getName())
                        .rollNo(e.getStudent().getUser() != null ? e.getStudent().getUser().getLoginId() : "N/A")
                        .className(e.getBatch().getCourse().getName())
                        .batchName(e.getBatch().getName())
                        .feeType(inst.getLabel())
                        .totalFee(amount)
                        .paidAmount(paid)
                        .balanceAmount(balance)
                        .dueDate(inst.getDueDate())
                        .status(compStatus)
                        .build());
            }
        }
        
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), components.size());
        List<FeeComponentResponse> pageContent = start <= end ? components.subList(start, end) : new ArrayList<>();
        
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, components.size());
    }

    @Transactional(readOnly = true)
    public Page<StudentFeeSummaryResponse> getStudentFeeSummaries(UUID branchId, String search, Pageable pageable) {
        List<StudentEnrolment> enrolments = enrolmentRepository.findAll().stream()
                .filter(e -> !e.isDeleted())
                .filter(e -> branchId == null || e.getBatch().getBranch().getId().equals(branchId))
                .filter(e -> search == null || e.getStudent().getName().toLowerCase().contains(search.toLowerCase()) || 
                             (e.getStudent().getUser() != null && e.getStudent().getUser().getLoginId().toLowerCase().contains(search.toLowerCase())))
                .toList();

        List<StudentFeeSummaryResponse> summaries = new ArrayList<>();
        
        for (StudentEnrolment e : enrolments) {
            BigDecimal totalFee = BigDecimal.ZERO;
            BigDecimal paidAmount = BigDecimal.ZERO;
            
            for (EnrolmentInstalment inst : e.getInstalments()) {
                if (inst.isDeleted()) continue;
                totalFee = totalFee.add(inst.getAmount());
                paidAmount = paidAmount.add(inst.getPaidAmount() != null ? inst.getPaidAmount() : BigDecimal.ZERO);
            }
            
            BigDecimal balance = totalFee.subtract(paidAmount);
            
            String status = "Pending";
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                status = "Paid";
            } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                status = "Partial";
            } else {
                // Check if any instalment is overdue
                boolean hasOverdue = e.getInstalments().stream()
                        .filter(i -> !i.isDeleted())
                        .anyMatch(i -> i.getDueDate() != null && i.getDueDate().isBefore(LocalDate.now()) && 
                                       i.getAmount().subtract(i.getPaidAmount() != null ? i.getPaidAmount() : BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0);
                if (hasOverdue) {
                    status = "Overdue";
                }
            }
            
            summaries.add(StudentFeeSummaryResponse.builder()
                    .enrolmentId(e.getId())
                    .studentId(e.getStudent().getId())
                    .studentName(e.getStudent().getName())
                    .rollNo(e.getStudent().getUser() != null ? e.getStudent().getUser().getLoginId() : "N/A")
                    .className(e.getBatch().getCourse().getName())
                    .batchName(e.getBatch().getName())
                    .totalFee(totalFee)
                    .paidAmount(paidAmount)
                    .balanceAmount(balance)
                    .status(status)
                    .build());
        }

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), summaries.size());
        List<StudentFeeSummaryResponse> pageContent = start <= end ? summaries.subList(start, end) : new ArrayList<>();
        
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, summaries.size());
    }

    @Transactional(readOnly = true)
    public FeeSummaryResponse getFeeSummaryByEnrolment(UUID enrolmentId) {
        StudentEnrolment enrolment = enrolmentRepository.findByIdAndIsDeletedFalse(enrolmentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentEnrolment", enrolmentId));

        BigDecimal totalFeesYearly = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal balanceFees = BigDecimal.ZERO;
        BigDecimal dueFees = BigDecimal.ZERO;

        List<FeeComponentResponse> components = new ArrayList<>();

        for (EnrolmentInstalment inst : enrolment.getInstalments()) {
            if (inst.isDeleted()) continue;

            BigDecimal amount = inst.getAmount();
            BigDecimal paid = inst.getPaidAmount() != null ? inst.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal balance = amount.subtract(paid);

            totalFeesYearly = totalFeesYearly.add(amount);
            totalPaid = totalPaid.add(paid);
            balanceFees = balanceFees.add(balance);

            if (inst.getDueDate() != null && inst.getDueDate().isBefore(LocalDate.now()) && balance.compareTo(BigDecimal.ZERO) > 0) {
                dueFees = dueFees.add(balance);
            }

            components.add(FeeComponentResponse.builder()
                    .id(inst.getId())
                    .enrolmentId(enrolment.getId())
                    .feeType(inst.getLabel())
                    .totalFee(amount)
                    .paidAmount(paid)
                    .balanceAmount(balance)
                    .dueDate(inst.getDueDate())
                    .status(determineStatus(amount, paid, inst.getDueDate()))
                    .build());
        }

        List<FeeTransactionResponse> transactionResponses = feeTransactionRepository.findByEnrolment_IdAndIsDeletedFalseOrderByTransactionDateDesc(enrolmentId).stream()
                .map(t -> FeeTransactionResponse.builder()
                        .id(t.getId())
                        .transactionDate(t.getTransactionDate())
                        .paymentMode(t.getPaymentMode())
                        .referenceNo(t.getReferenceNo())
                        .remarks(t.getRemarks())
                        .amountReceived(t.getAmountReceived())
                        .discountAmount(t.getDiscountAmount())
                        .lateFeeAmount(t.getLateFeeAmount())
                        .recordedBy(t.getCreatedByUser() != null ? t.getCreatedByUser().getFullName() : "System")
                        .build())
                .toList();

        return FeeSummaryResponse.builder()
                .enrolmentId(enrolment.getId())
                .studentName(enrolment.getStudent().getName())
                .rollNo(enrolment.getStudent().getUser() != null ? enrolment.getStudent().getUser().getLoginId() : "N/A")
                .className(enrolment.getBatch().getCourse().getName())
                .batchName(enrolment.getBatch().getName())
                .phone(enrolment.getStudent().getMobile())
                .totalFeesYearly(totalFeesYearly)
                .totalPaid(totalPaid)
                .balanceFees(balanceFees)
                .dueFees(dueFees)
                .feeComponents(components)
                .transactions(transactionResponses)
                .build();
    }

    @Transactional
    public FeeSummaryResponse collectFees(UUID enrolmentId, CollectFeeRequest request) {
        StudentEnrolment enrolment = enrolmentRepository.findByIdAndIsDeletedFalse(enrolmentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentEnrolment", enrolmentId));

        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByLoginIdIgnoreCaseAndIsDeletedFalse(loginId).orElse(null);

        // 1. Process New Charges (add them as new instalments)
        if (request.getNewCharges() != null) {
            for (CollectFeeRequest.NewChargeRequest newCharge : request.getNewCharges()) {
                EnrolmentInstalment chargeInstalment = EnrolmentInstalment.builder()
                        .enrolment(enrolment)
                        .instalmentNumber(enrolment.getInstalments().size() + 1)
                        .label(newCharge.getLabel())
                        .amount(newCharge.getAmount())
                        .dueDate(request.getPaymentDate())
                        .paidAmount(BigDecimal.ZERO)
                        .isPaid(false)
                        .build();
                enrolment.getInstalments().add(chargeInstalment);
                
                // Automatically allocate this new charge to be paid
                if (request.getAllocations() == null) {
                    request.setAllocations(new ArrayList<>());
                }
                
                // We don't have ID yet, but we'll map it by object ref later.
                // Since it's easier, let's save the enrolment first to generate IDs for new instalments if needed,
                // But JPA handles it if we just iterate correctly.
            }
        }

        // 2. Validate Allocations
        BigDecimal totalAllocated = BigDecimal.ZERO;
        List<FeeTransactionDetail> transactionDetails = new ArrayList<>();
        
        FeeTransaction transaction = FeeTransaction.builder()
                .enrolment(enrolment)
                .transactionDate(request.getPaymentDate().atStartOfDay())
                .paymentMode(request.getPaymentMode())
                .referenceNo(request.getReferenceNo())
                .remarks(request.getRemarks())
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .lateFeeAmount(request.getLateFeeAmount() != null ? request.getLateFeeAmount() : BigDecimal.ZERO)
                .amountReceived(request.getAmountReceived())
                .createdByUser(currentUser)
                .build();

        if (request.getAllocations() != null) {
            for (CollectFeeRequest.AllocationRequest alloc : request.getAllocations()) {
                EnrolmentInstalment instalment = enrolment.getInstalments().stream()
                        .filter(i -> i.getId() != null && i.getId().equals(alloc.getInstalmentId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessRuleException("Instalment not found", "INVALID_INSTALMENT"));

                BigDecimal currentPaid = instalment.getPaidAmount() != null ? instalment.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal balance = instalment.getAmount().subtract(currentPaid);
                
                if (alloc.getAmount().compareTo(balance) > 0) {
                    throw new BusinessRuleException("Allocation exceeds balance for " + instalment.getLabel(), "ALLOCATION_EXCEEDS_BALANCE");
                }

                instalment.setPaidAmount(currentPaid.add(alloc.getAmount()));
                if (instalment.getPaidAmount().compareTo(instalment.getAmount()) >= 0) {
                    instalment.setIsPaid(true);
                    instalment.setPaidDate(request.getPaymentDate());
                }

                totalAllocated = totalAllocated.add(alloc.getAmount());

                transactionDetails.add(FeeTransactionDetail.builder()
                        .transaction(transaction)
                        .instalment(instalment)
                        .allocatedAmount(alloc.getAmount())
                        .build());
            }
        }
        
        // Handle new charges allocation immediately
        if (request.getNewCharges() != null) {
             for (EnrolmentInstalment inst : enrolment.getInstalments()) {
                 if (inst.getId() == null) { // It's a new charge
                     inst.setPaidAmount(inst.getAmount());
                     inst.setIsPaid(true);
                     inst.setPaidDate(request.getPaymentDate());
                     totalAllocated = totalAllocated.add(inst.getAmount());
                     transactionDetails.add(FeeTransactionDetail.builder()
                             .transaction(transaction)
                             .instalment(inst)
                             .allocatedAmount(inst.getAmount())
                             .build());
                 }
             }
        }

        transaction.setTotalSelectedAmount(totalAllocated);
        BigDecimal payable = totalAllocated.subtract(transaction.getDiscountAmount()).add(transaction.getLateFeeAmount());
        transaction.setTotalPayableAmount(payable);
        transaction.setDetails(transactionDetails);

        enrolmentRepository.save(enrolment);
        feeTransactionRepository.save(transaction);

        return getFeeSummaryByEnrolment(enrolmentId);
    }

    private String determineStatus(BigDecimal total, BigDecimal paid, LocalDate dueDate) {
        if (paid.compareTo(total) >= 0) return "Paid";
        if (paid.compareTo(BigDecimal.ZERO) > 0) return "Partial";
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) return "Overdue";
        return "Pending";
    }

    @Transactional(readOnly = true)
    public FeeAnalyticsResponse getFeeAnalytics(UUID branchId, String month) {
        List<StudentEnrolment> enrolments = enrolmentRepository.findAll().stream()
                .filter(e -> !e.isDeleted())
                .filter(e -> branchId == null || e.getBatch().getBranch().getId().equals(branchId))
                .toList();

        BigDecimal totalExpected = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        List<UUID> overdueStudents = new ArrayList<>();
        List<UUID> allStudents = new ArrayList<>();

        // Group by fee type
        java.util.Map<String, BigDecimal> feeTypeTotals = new java.util.HashMap<>();

        // Optionally filter by month if required (e.g. dueDate falls in month). We'll assume the whole year if month is null.
        // For simplicity, month filtering is skipped unless explicitly requested, but usually dashboards show yearly or current active.
        
        for (StudentEnrolment e : enrolments) {
            allStudents.add(e.getStudent().getId());
            boolean studentHasOverdue = false;
            
            for (EnrolmentInstalment inst : e.getInstalments()) {
                if (inst.isDeleted()) continue;

                // Simple month filter based on yyyy-MM
                if (month != null && !month.isBlank()) {
                    if (inst.getDueDate() == null || !inst.getDueDate().toString().startsWith(month)) {
                        continue;
                    }
                }

                BigDecimal amount = inst.getAmount();
                BigDecimal paid = inst.getPaidAmount() != null ? inst.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal balance = amount.subtract(paid);

                totalExpected = totalExpected.add(amount);
                totalCollected = totalCollected.add(paid);
                
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    if (inst.getDueDate() != null && inst.getDueDate().isBefore(LocalDate.now())) {
                        totalOverdue = totalOverdue.add(balance);
                        studentHasOverdue = true;
                    } else {
                        totalPending = totalPending.add(balance);
                    }
                }

                feeTypeTotals.put(inst.getLabel(), feeTypeTotals.getOrDefault(inst.getLabel(), BigDecimal.ZERO).add(amount));
            }
            if (studentHasOverdue) {
                overdueStudents.add(e.getStudent().getId());
            }
        }

        List<FeeAnalyticsResponse.FeeTypeSummary> summaries = feeTypeTotals.entrySet().stream()
                .map(entry -> FeeAnalyticsResponse.FeeTypeSummary.builder()
                        .feeType(entry.getKey())
                        .totalAmount(entry.getValue())
                        .build())
                .toList();

        return FeeAnalyticsResponse.builder()
                .totalExpected(totalExpected)
                .totalCollected(totalCollected)
                .totalPending(totalPending)
                .totalOverdue(totalOverdue)
                .totalStudents((int) allStudents.stream().distinct().count())
                .overdueStudentsCount((int) overdueStudents.stream().distinct().count())
                .feeTypeSummaries(summaries)
                .build();
    }
}
