package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateAdminRequest;
import com.classmanager.cms_backend.dto.request.UpdateAdminRequest;
import com.classmanager.cms_backend.dto.request.UpdateAdminStatusRequest;
import com.classmanager.cms_backend.dto.response.AdminManagementResponse;
import com.classmanager.cms_backend.dto.response.AdminResponse;
import com.classmanager.cms_backend.entity.AdminProfile;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.OperationalRecord;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.AdminProfileRepository;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.OperationalRecordRepository;
import com.classmanager.cms_backend.repository.RefreshTokenRepository;
import com.classmanager.cms_backend.repository.RoleRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminAdminService {

    private static final String MODULE_ACTIVITY = "ACTIVITY";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String BRANCH_LABEL_ALL = "All Branches";

    private final AdminProfileRepository adminProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OperationalRecordRepository operationalRecordRepository;

    @Transactional(readOnly = true)
    public AdminManagementResponse getAdmins(String search, Boolean isActive, UUID branchId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<AdminProfile> adminPage = adminProfileRepository.searchAdminProfiles(buildSearchPattern(search), isActive, branchId, pageable);

        return AdminManagementResponse.builder()
                .summary(AdminManagementResponse.Summary.builder()
                        .totalAdmins(adminProfileRepository.countTotalAdmins(branchId))
                        .activeAdmins(adminProfileRepository.countActiveAdmins(branchId))
                        .inactiveAdmins(adminProfileRepository.countInactiveAdmins(branchId))
                        .allBranchAdmins(adminProfileRepository.countAllBranchAdmins())
                        .build())
                .content(adminPage.getContent().stream().map(this::toResponse).toList())
                .page(AdminManagementResponse.PageMeta.builder()
                        .pageNumber(adminPage.getNumber())
                        .pageSize(adminPage.getSize())
                        .totalElements(adminPage.getTotalElements())
                        .totalPages(adminPage.getTotalPages())
                        .first(adminPage.isFirst())
                        .last(adminPage.isLast())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdmin(UUID adminId) {
        return toResponse(loadAdminProfile(adminId));
    }

    @Transactional
    public AdminResponse createAdmin(CreateAdminRequest request, UUID createdByUserId) {
        validateAdminRole(request.getRole());
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());

        String normalizedEmail = normalizeRequired(request.getEmail(), "Email is required");
        String normalizedLoginId = normalizeRequired(request.getLoginId(), "Login ID is required");

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(normalizedEmail)) {
            throw new ResourceAlreadyExistsException("A user with this email already exists.");
        }
        if (userRepository.existsByLoginIdIgnoreCaseAndIsDeletedFalse(normalizedLoginId)) {
            throw new ResourceAlreadyExistsException("A user with this login ID already exists.");
        }

        Branch branch = resolveBranch(request.getBranchId());
        boolean allBranchesAccess = resolveAllBranchesAccess(request.getAllBranchesAccess(), branch);
        Role adminRole = loadAdminRole();
        User creator = loadUser(createdByUserId);

        User user = User.builder()
                .email(normalizedEmail)
                .loginId(normalizedLoginId)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone().trim())
                .profilePhotoUrl(trimToNull(request.getProfilePhotoUrl()))
                .branch(allBranchesAccess ? null : branch)
                .isActive(true)
                .roles(Set.of(adminRole))
                .build();
        user = userRepository.save(user);

        AdminProfile adminProfile = AdminProfile.builder()
                .user(user)
                .dateOfBirth(request.getDateOfBirth())
                .gender(trimToNull(request.getGender()))
                .roleTitle(request.getRole().trim())
                .joiningDate(request.getJoiningDate())
                .accessLevel(request.getAccessLevel().trim())
                .address(trimToNull(request.getAddress()))
                .allBranchesAccess(allBranchesAccess)
                .createdByUser(creator)
                .build();
        adminProfile = adminProfileRepository.save(adminProfile);

        recordAdminActivity("ADMIN_CREATED", user.getFullName(), creator.getId(), branch,
                "Admin account created with access level " + adminProfile.getAccessLevel());
        return toResponse(adminProfile);
    }

    @Transactional
    public AdminResponse updateAdmin(UUID adminId, UpdateAdminRequest request, UUID updatedByUserId) {
        AdminProfile adminProfile = loadAdminProfile(adminId);
        User user = adminProfile.getUser();

        validateAdminRole(request.getRole());
        validateUniqueEmailForUpdate(user.getId(), request.getEmail());
        validateUniqueLoginIdForUpdate(user.getId(), request.getLoginId());
        handleOptionalPasswordUpdate(user, request.getPassword(), request.getConfirmPassword());

        Branch branch = resolveBranch(request.getBranchId());
        boolean allBranchesAccess = resolveAllBranchesAccess(request.getAllBranchesAccess(), branch);

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizeRequired(request.getEmail(), "Email is required"));
        user.setPhone(request.getPhone().trim());
        user.setLoginId(normalizeRequired(request.getLoginId(), "Login ID is required"));
        user.setProfilePhotoUrl(trimToNull(request.getProfilePhotoUrl()));
        user.setBranch(allBranchesAccess ? null : branch);
        userRepository.save(user);

        adminProfile.setDateOfBirth(request.getDateOfBirth());
        adminProfile.setGender(trimToNull(request.getGender()));
        adminProfile.setRoleTitle(request.getRole().trim());
        adminProfile.setJoiningDate(request.getJoiningDate());
        adminProfile.setAccessLevel(request.getAccessLevel().trim());
        adminProfile.setAddress(trimToNull(request.getAddress()));
        adminProfile.setAllBranchesAccess(allBranchesAccess);
        adminProfileRepository.save(adminProfile);

        recordAdminActivity("ADMIN_UPDATED", user.getFullName(), updatedByUserId, branch,
                "Admin profile updated");
        return toResponse(adminProfile);
    }

    @Transactional
    public AdminResponse updateAdminStatus(UUID adminId, UpdateAdminStatusRequest request, UUID updatedByUserId) {
        AdminProfile adminProfile = loadAdminProfile(adminId);
        User user = adminProfile.getUser();

        user.setIsActive(request.getIsActive());
        userRepository.save(user);

        if (!Boolean.TRUE.equals(request.getIsActive())) {
            refreshTokenRepository.revokeAllForUser(user.getId(), java.time.LocalDateTime.now(), "ADMIN_DEACTIVATED");
        }

        String description = Boolean.TRUE.equals(request.getIsActive())
                ? "Admin account activated"
                : "Admin account deactivated" + (StringUtils.hasText(request.getReason()) ? ": " + request.getReason().trim() : "");
        recordAdminActivity(Boolean.TRUE.equals(request.getIsActive()) ? "ADMIN_ACTIVATED" : "ADMIN_DEACTIVATED",
                user.getFullName(), updatedByUserId, user.getBranch(), description);
        return toResponse(adminProfile);
    }

    @Transactional
    public void deleteAdmin(UUID adminId, UUID deletedByUserId) {
        AdminProfile adminProfile = loadAdminProfile(adminId);
        User user = adminProfile.getUser();

        adminProfile.softDelete();
        user.softDelete();
        user.setIsActive(false);

        adminProfileRepository.save(adminProfile);
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId(), java.time.LocalDateTime.now(), "ADMIN_DELETED");

        recordAdminActivity("ADMIN_DELETED", user.getFullName(), deletedByUserId, user.getBranch(),
                "Admin account soft deleted");
    }

    private AdminProfile loadAdminProfile(UUID adminId) {
        return adminProfileRepository.findActiveById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile", adminId));
    }

    private User loadUser(UUID userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Role loadAdminRole() {
        return roleRepository.findByName(UserRole.ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role", UserRole.ADMIN.name()));
    }

    private Branch resolveBranch(UUID branchId) {
        if (branchId == null) {
            return null;
        }
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private boolean resolveAllBranchesAccess(Boolean allBranchesAccess, Branch branch) {
        if (Boolean.TRUE.equals(allBranchesAccess)) {
            return true;
        }
        if (allBranchesAccess == null) {
            return branch == null;
        }
        if (!allBranchesAccess && branch == null) {
            throw new BadRequestException("Branch is required when all-branches access is false", "BRANCH_REQUIRED");
        }
        return false;
    }

    private void validateAdminRole(String role) {
        if (!StringUtils.hasText(role) || !ROLE_ADMIN.equalsIgnoreCase(role.trim())) {
            throw new BadRequestException("Only ADMIN role can be created from admin management", "INVALID_ADMIN_ROLE");
        }
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (!StringUtils.hasText(password) || !StringUtils.hasText(confirmPassword) || !password.equals(confirmPassword)) {
            throw new BadRequestException("Password and confirm password do not match", "PASSWORD_MISMATCH");
        }
    }

    private void handleOptionalPasswordUpdate(User user, String password, String confirmPassword) {
        if (!StringUtils.hasText(password) && !StringUtils.hasText(confirmPassword)) {
            return;
        }
        validatePasswordConfirmation(password, confirmPassword);
        user.setPasswordHash(passwordEncoder.encode(password));
        refreshTokenRepository.revokeAllForUser(user.getId(), java.time.LocalDateTime.now(), "PASSWORD_CHANGED_BY_SUPER_ADMIN");
    }

    private void validateUniqueEmailForUpdate(UUID userId, String email) {
        String normalizedEmail = normalizeRequired(email, "Email is required");
        userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(normalizedEmail)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("A user with this email already exists.");
                });
    }

    private void validateUniqueLoginIdForUpdate(UUID userId, String loginId) {
        String normalizedLoginId = normalizeRequired(loginId, "Login ID is required");
        userRepository.findByLoginIdIgnoreCaseAndIsDeletedFalse(normalizedLoginId)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("A user with this login ID already exists.");
                });
    }

    private AdminResponse toResponse(AdminProfile adminProfile) {
        User user = adminProfile.getUser();
        boolean allBranchesAccess = Boolean.TRUE.equals(adminProfile.getAllBranchesAccess());
        Branch branch = user.getBranch();

        return AdminResponse.builder()
                .id(adminProfile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .loginId(user.getLoginId())
                .dateOfBirth(adminProfile.getDateOfBirth())
                .gender(adminProfile.getGender())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .role(adminProfile.getRoleTitle())
                .joiningDate(adminProfile.getJoiningDate())
                .accessLevel(adminProfile.getAccessLevel())
                .address(adminProfile.getAddress())
                .allBranchesAccess(allBranchesAccess)
                .branchId(branch != null ? branch.getId() : null)
                .branchName(allBranchesAccess || branch == null ? BRANCH_LABEL_ALL : branch.getName())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(adminProfile.getCreatedAt())
                .updatedAt(adminProfile.getUpdatedAt())
                .build();
    }

    private void recordAdminActivity(String type, String adminName, UUID actorUserId, Branch branch, String description) {
        OperationalRecord record = OperationalRecord.builder()
                .module(MODULE_ACTIVITY)
                .type(type)
                .title(adminName)
                .description(description)
                .branch(branch)
                .createdByUserId(actorUserId)
                .eventDate(LocalDate.now())
                .status("COMPLETED")
                .build();
        operationalRecordRepository.save(record);
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String buildSearchPattern(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : "%" + normalized + "%";
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message, "VALIDATION_ERROR");
        }
        return value.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
