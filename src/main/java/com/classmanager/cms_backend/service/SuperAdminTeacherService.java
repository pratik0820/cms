package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateTeacherRequest;
import com.classmanager.cms_backend.dto.request.UpdateTeacherRequest;
import com.classmanager.cms_backend.dto.request.UpdateTeacherStatusRequest;
import com.classmanager.cms_backend.dto.response.TeacherManagementResponse;
import com.classmanager.cms_backend.dto.response.TeacherResponse;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.OperationalRecord;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.entity.Teacher;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.OperationalRecordRepository;
import com.classmanager.cms_backend.repository.RefreshTokenRepository;
import com.classmanager.cms_backend.repository.RoleRepository;
import com.classmanager.cms_backend.repository.SubjectRepository;
import com.classmanager.cms_backend.repository.TeacherRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminTeacherService {

    private static final String MODULE_ACTIVITY = "ACTIVITY";

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OperationalRecordRepository operationalRecordRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public TeacherManagementResponse getTeachers(String search,
                                                 Boolean isActive,
                                                 UUID branchId,
                                                 String subject,
                                                 UUID subjectId,
                                                 int page,
                                                 int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Teacher> teacherPage = teacherRepository.searchTeachers(
                buildSearchPattern(search),
                isActive,
                branchId,
                trimToNull(subject),
                subjectId,
                pageable
        );

        return TeacherManagementResponse.builder()
                .summary(TeacherManagementResponse.Summary.builder()
                        .totalTeachers(teacherRepository.countTotalTeachers(branchId))
                        .activeTeachers(teacherRepository.countActiveTeachers(branchId))
                        .inactiveTeachers(teacherRepository.countInactiveTeachers(branchId))
                        .build())
                .content(teacherPage.getContent().stream().map(this::toResponse).toList())
                .page(TeacherManagementResponse.PageMeta.builder()
                        .pageNumber(teacherPage.getNumber())
                        .pageSize(teacherPage.getSize())
                        .totalElements(teacherPage.getTotalElements())
                        .totalPages(teacherPage.getTotalPages())
                        .first(teacherPage.isFirst())
                        .last(teacherPage.isLast())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacher(UUID teacherId) {
        return toResponse(loadTeacher(teacherId));
    }

    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request, UUID createdByUserId) {
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());

        String normalizedEmail = normalizeRequired(request.getEmail(), "Email is required");
        String normalizedLoginId = normalizeRequired(request.getLoginId(), "Login ID is required");

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(normalizedEmail)) {
            throw new ResourceAlreadyExistsException("A user with this email already exists.");
        }
        if (userRepository.existsByLoginIdIgnoreCaseAndIsDeletedFalse(normalizedLoginId)) {
            throw new ResourceAlreadyExistsException("A user with this login ID already exists.");
        }

        Branch branch = loadBranch(request.getBranchId());
        Role teacherRole = loadTeacherRole();
        User creator = loadUser(createdByUserId);
        List<Subject> catalogSubjects = resolveCatalogSubjects(request.getSubjectIds());
        List<String> subjects = resolveSubjectNames(request.getSubjects(), catalogSubjects);

        User user = User.builder()
                .email(normalizedEmail)
                .loginId(normalizedLoginId)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone().trim())
                .profilePhotoUrl(trimToNull(request.getProfilePhotoUrl()))
                .branch(branch)
                .isActive(true)
                .roles(Set.of(teacherRole))
                .build();
        user = userRepository.save(user);

        Teacher teacher = Teacher.builder()
                .user(user)
                .branch(branch)
                .name(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .dateOfBirth(request.getDateOfBirth())
                .gender(trimToNull(request.getGender()))
                .qualification(request.getQualification().trim())
                .experienceYears(request.getExperienceYears())
                .subjects(subjects)
                .catalogSubjects(catalogSubjects)
                .specialization(trimToNull(request.getSpecialization()))
                .joiningDate(request.getJoiningDate())
                .employmentType(request.getEmploymentType().trim())
                .salaryType(trimToNull(request.getSalaryType()))
                .hourlyRate(resolveHourlyRate(request.getHourlyRate()))
                .address(trimToNull(request.getAddress()))
                .isActive(true)
                .createdByUser(creator)
                .build();
        teacher = teacherRepository.save(teacher);

        recordTeacherActivity("TEACHER_CREATED", teacher, createdByUserId, "Teacher account created");
        return toResponse(teacher);
    }

    @Transactional
    public TeacherResponse updateTeacher(UUID teacherId, UpdateTeacherRequest request, UUID updatedByUserId) {
        Teacher teacher = loadTeacher(teacherId);
        User user = teacher.getUser();

        validateUniqueEmailForUpdate(user.getId(), request.getEmail());
        validateUniqueLoginIdForUpdate(user.getId(), request.getLoginId());
        handleOptionalPasswordUpdate(user, request.getPassword(), request.getConfirmPassword());

        Branch branch = loadBranch(request.getBranchId());
        List<Subject> catalogSubjects = resolveCatalogSubjects(request.getSubjectIds());
        List<String> subjects = resolveSubjectNames(request.getSubjects(), catalogSubjects);

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizeRequired(request.getEmail(), "Email is required"));
        user.setPhone(request.getPhone().trim());
        user.setLoginId(normalizeRequired(request.getLoginId(), "Login ID is required"));
        user.setProfilePhotoUrl(trimToNull(request.getProfilePhotoUrl()));
        user.setBranch(branch);
        userRepository.save(user);

        teacher.setBranch(branch);
        teacher.setName(user.getFullName());
        teacher.setPhone(user.getPhone());
        teacher.setEmail(user.getEmail());
        teacher.setDateOfBirth(request.getDateOfBirth());
        teacher.setGender(trimToNull(request.getGender()));
        teacher.setQualification(request.getQualification().trim());
        teacher.setExperienceYears(request.getExperienceYears());
        teacher.setSubjects(subjects);
        teacher.setCatalogSubjects(catalogSubjects);
        teacher.setSpecialization(trimToNull(request.getSpecialization()));
        teacher.setJoiningDate(request.getJoiningDate());
        teacher.setEmploymentType(request.getEmploymentType().trim());
        teacher.setSalaryType(trimToNull(request.getSalaryType()));
        teacher.setHourlyRate(resolveHourlyRate(request.getHourlyRate()));
        teacher.setAddress(trimToNull(request.getAddress()));
        teacher = teacherRepository.save(teacher);

        recordTeacherActivity("TEACHER_UPDATED", teacher, updatedByUserId, "Teacher profile updated");
        return toResponse(teacher);
    }

    @Transactional
    public TeacherResponse updateTeacherStatus(UUID teacherId, UpdateTeacherStatusRequest request, UUID updatedByUserId) {
        Teacher teacher = loadTeacher(teacherId);
        User user = teacher.getUser();

        teacher.setIsActive(request.getIsActive());
        user.setIsActive(request.getIsActive());
        teacherRepository.save(teacher);
        userRepository.save(user);

        if (!Boolean.TRUE.equals(request.getIsActive())) {
            refreshTokenRepository.revokeAllForUser(user.getId(), LocalDateTime.now(), "TEACHER_DEACTIVATED");
        }

        String description = Boolean.TRUE.equals(request.getIsActive())
                ? "Teacher account activated"
                : "Teacher account deactivated" + (StringUtils.hasText(request.getReason()) ? ": " + request.getReason().trim() : "");
        recordTeacherActivity(Boolean.TRUE.equals(request.getIsActive()) ? "TEACHER_ACTIVATED" : "TEACHER_DEACTIVATED",
                teacher, updatedByUserId, description);
        return toResponse(teacher);
    }

    @Transactional
    public void deleteTeacher(UUID teacherId, UUID deletedByUserId) {
        Teacher teacher = loadTeacher(teacherId);
        User user = teacher.getUser();

        teacher.softDelete();
        teacher.setIsActive(false);
        user.softDelete();
        user.setIsActive(false);

        teacherRepository.save(teacher);
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId(), LocalDateTime.now(), "TEACHER_DELETED");

        recordTeacherActivity("TEACHER_DELETED", teacher, deletedByUserId, "Teacher account soft deleted");
    }

    private Teacher loadTeacher(UUID teacherId) {
        return teacherRepository.findActiveById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));
    }

    private Branch loadBranch(UUID branchId) {
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private User loadUser(UUID userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Role loadTeacherRole() {
        return roleRepository.findByName(UserRole.TEACHER.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role", UserRole.TEACHER.name()));
    }

    private TeacherResponse toResponse(Teacher teacher) {
        User user = teacher.getUser();
        Branch branch = teacher.getBranch();

        return TeacherResponse.builder()
                .id(teacher.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .loginId(user.getLoginId())
                .dateOfBirth(teacher.getDateOfBirth())
                .gender(teacher.getGender())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .qualification(teacher.getQualification())
                .experienceYears(teacher.getExperienceYears())
                .subjects(resolveResponseSubjectNames(teacher))
                .subjectIds(teacher.getCatalogSubjects().stream().map(Subject::getId).toList())
                .specialization(teacher.getSpecialization())
                .joiningDate(teacher.getJoiningDate())
                .employmentType(teacher.getEmploymentType())
                .salaryType(teacher.getSalaryType())
                .hourlyRate(teacher.getHourlyRate())
                .address(teacher.getAddress())
                .branchId(branch.getId())
                .branchName(branch.getName())
                .isActive(teacher.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
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
        refreshTokenRepository.revokeAllForUser(user.getId(), LocalDateTime.now(), "PASSWORD_CHANGED_BY_SUPER_ADMIN");
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

    private List<Subject> resolveCatalogSubjects(List<UUID> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return List.of();
        }
        return subjectIds.stream()
                .distinct()
                .map(subjectId -> subjectRepository.findByIdAndIsDeletedFalse(subjectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId)))
                .toList();
    }

    private List<String> resolveSubjectNames(List<String> legacySubjects, List<Subject> catalogSubjects) {
        List<String> normalizedSubjects = legacySubjects == null
                ? List.of()
                : legacySubjects.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();

        if (!catalogSubjects.isEmpty()) {
            return catalogSubjects.stream()
                    .map(Subject::getDisplayName)
                    .distinct()
                    .toList();
        }
        if (normalizedSubjects.isEmpty()) {
            throw new BadRequestException("At least one subject is required", "VALIDATION_ERROR");
        }
        return normalizedSubjects;
    }

    private List<String> resolveResponseSubjectNames(Teacher teacher) {
        if (teacher.getCatalogSubjects() != null && !teacher.getCatalogSubjects().isEmpty()) {
            return teacher.getCatalogSubjects().stream()
                    .map(Subject::getDisplayName)
                    .toList();
        }
        return List.copyOf(teacher.getSubjects());
    }

    private BigDecimal resolveHourlyRate(BigDecimal hourlyRate) {
        return hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
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

    private void recordTeacherActivity(String type, Teacher teacher, UUID actorUserId, String description) {
        OperationalRecord record = OperationalRecord.builder()
                .module(MODULE_ACTIVITY)
                .type(type)
                .title(teacher.getName())
                .description(description)
                .branch(teacher.getBranch())
                .teacherId(teacher.getId())
                .createdByUserId(actorUserId)
                .eventDate(LocalDate.now())
                .status("COMPLETED")
                .build();
        operationalRecordRepository.save(record);
    }
}
