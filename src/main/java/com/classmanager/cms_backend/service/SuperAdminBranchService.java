package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.SuperAdminDashboardResponse;
import com.classmanager.cms_backend.dto.request.CreateBranchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBranchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBranchStatusRequest;
import com.classmanager.cms_backend.dto.response.BranchManagementResponse;
import com.classmanager.cms_backend.dto.response.BranchResponse;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.OperationalRecord;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.OperationalRecordRepository;
import com.classmanager.cms_backend.repository.StudentRepository;
import com.classmanager.cms_backend.repository.TeacherRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminBranchService {

    private static final String MODULE_ACTIVITY = "ACTIVITY";

    private final BranchRepository branchRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final OperationalRecordRepository operationalRecordRepository;

    @Transactional(readOnly = true)
    public List<SuperAdminDashboardResponse.BranchOption> getBranchOptions() {
        return branchRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc().stream()
                .map(branch -> SuperAdminDashboardResponse.BranchOption.builder()
                        .id(branch.getId())
                        .name(branch.getName())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public BranchManagementResponse getBranches(String search, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Branch> branchPage = branchRepository.searchBranches(buildSearchPattern(search), isActive, pageable);

        return BranchManagementResponse.builder()
                .summary(BranchManagementResponse.Summary.builder()
                        .totalBranches(branchRepository.countByIsDeletedFalseAndOptionalStatus(null))
                        .activeBranches(branchRepository.countByIsDeletedFalseAndOptionalStatus(true))
                        .inactiveBranches(branchRepository.countByIsDeletedFalseAndOptionalStatus(false))
                        .build())
                .content(branchPage.getContent().stream().map(this::toResponse).toList())
                .page(BranchManagementResponse.PageMeta.builder()
                        .pageNumber(branchPage.getNumber())
                        .pageSize(branchPage.getSize())
                        .totalElements(branchPage.getTotalElements())
                        .totalPages(branchPage.getTotalPages())
                        .first(branchPage.isFirst())
                        .last(branchPage.isLast())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranch(UUID branchId) {
        return toResponse(loadBranch(branchId));
    }

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request, UUID createdByUserId) {
        validateUniqueBranchName(request.getName(), null);

        Branch branch = Branch.builder()
                .name(request.getName().trim())
                .address(trimToNull(request.getAddress()))
                .city(trimToNull(request.getCity()))
                .phone(trimToNull(request.getPhone()))
                .email(normalizeEmail(request.getEmail()))
                .isActive(true)
                .build();

        branch = branchRepository.save(branch);
        recordBranchActivity("BRANCH_CREATED", branch, createdByUserId, "Branch created");
        return toResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranch(UUID branchId, UpdateBranchRequest request, UUID updatedByUserId) {
        Branch branch = loadBranch(branchId);
        validateUniqueBranchName(request.getName(), branchId);

        branch.setName(request.getName().trim());
        branch.setAddress(trimToNull(request.getAddress()));
        branch.setCity(trimToNull(request.getCity()));
        branch.setPhone(trimToNull(request.getPhone()));
        branch.setEmail(normalizeEmail(request.getEmail()));
        branch = branchRepository.save(branch);

        recordBranchActivity("BRANCH_UPDATED", branch, updatedByUserId, "Branch updated");
        return toResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranchStatus(UUID branchId, UpdateBranchStatusRequest request, UUID updatedByUserId) {
        Branch branch = loadBranch(branchId);
        branch.setIsActive(request.getIsActive());
        branch = branchRepository.save(branch);

        String description = Boolean.TRUE.equals(request.getIsActive())
                ? "Branch activated"
                : "Branch deactivated" + (StringUtils.hasText(request.getReason()) ? ": " + request.getReason().trim() : "");
        recordBranchActivity(Boolean.TRUE.equals(request.getIsActive()) ? "BRANCH_ACTIVATED" : "BRANCH_DEACTIVATED",
                branch, updatedByUserId, description);
        return toResponse(branch);
    }

    @Transactional
    public void deleteBranch(UUID branchId, UUID deletedByUserId) {
        Branch branch = loadBranch(branchId);
        long linkedStudents = studentRepository.countByBranch_IdAndIsDeletedFalse(branchId);
        long linkedTeachers = teacherRepository.countByBranch_IdAndIsDeletedFalse(branchId);
        long linkedAdmins = userRepository.countByRoleNameAndBranchId(UserRole.ADMIN.name(), branchId);

        if (linkedStudents > 0 || linkedTeachers > 0 || linkedAdmins > 0) {
            throw new BadRequestException(
                    "Branch cannot be deleted while linked admins, teachers, or students still exist",
                    "BRANCH_IN_USE"
            );
        }

        branch.softDelete();
        branch.setIsActive(false);
        branchRepository.save(branch);
        recordBranchActivity("BRANCH_DELETED", branch, deletedByUserId, "Branch soft deleted");
    }

    private Branch loadBranch(UUID branchId) {
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private BranchResponse toResponse(Branch branch) {
        UUID branchId = branch.getId();
        return BranchResponse.builder()
                .id(branchId)
                .name(branch.getName())
                .address(branch.getAddress())
                .city(branch.getCity())
                .phone(branch.getPhone())
                .email(branch.getEmail())
                .isActive(branch.getIsActive())
                .totalStudents(studentRepository.countByBranch_IdAndIsDeletedFalse(branchId))
                .totalTeachers(teacherRepository.countByBranch_IdAndIsDeletedFalse(branchId))
                .totalAdmins(userRepository.countByRoleNameAndBranchId(UserRole.ADMIN.name(), branchId))
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }

    private void validateUniqueBranchName(String branchName, UUID currentBranchId) {
        if (!StringUtils.hasText(branchName)) {
            throw new BadRequestException("Branch name is required", "VALIDATION_ERROR");
        }

        String normalizedName = branchName.trim();
        List<Branch> branches = branchRepository.findByIsDeletedFalseOrderByNameAsc();
        boolean exists = branches.stream()
                .anyMatch(branch -> branch.getName() != null
                        && branch.getName().equalsIgnoreCase(normalizedName)
                        && (currentBranchId == null || !branch.getId().equals(currentBranchId)));
        if (exists) {
            throw new ResourceAlreadyExistsException("A branch with this name already exists.");
        }
    }

    private String buildSearchPattern(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return "%" + value.trim().toLowerCase() + "%";
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase() : null;
    }

    private void recordBranchActivity(String type, Branch branch, UUID actorUserId, String description) {
        OperationalRecord record = OperationalRecord.builder()
                .module(MODULE_ACTIVITY)
                .type(type)
                .title(branch.getName())
                .description(description)
                .branch(branch)
                .createdByUserId(actorUserId)
                .eventDate(LocalDate.now())
                .status("COMPLETED")
                .build();
        operationalRecordRepository.save(record);
    }
}
