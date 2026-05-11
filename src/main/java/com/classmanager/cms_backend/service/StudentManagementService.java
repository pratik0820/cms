package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateStudentRequest;
import com.classmanager.cms_backend.dto.request.UpdateStudentRequest;
import com.classmanager.cms_backend.dto.request.UpdateStudentStatusRequest;
import com.classmanager.cms_backend.dto.response.StudentManagementResponse;
import com.classmanager.cms_backend.dto.response.StudentResponse;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.OperationalRecord;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.entity.Student;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.OperationalRecordRepository;
import com.classmanager.cms_backend.repository.RefreshTokenRepository;
import com.classmanager.cms_backend.repository.RoleRepository;
import com.classmanager.cms_backend.repository.StudentRepository;
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
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentManagementService {

    private static final String MODULE_ACTIVITY = "ACTIVITY";

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OperationalRecordRepository operationalRecordRepository;

    @Transactional(readOnly = true)
    public StudentManagementResponse getStudents(String search,
                                                 Boolean isActive,
                                                 UUID branchId,
                                                 String standard,
                                                 String batch,
                                                 int page,
                                                 int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Student> studentPage = studentRepository.searchStudents(
                buildSearchPattern(search),
                isActive,
                branchId,
                trimToNull(standard),
                trimToNull(batch),
                pageable
        );

        return StudentManagementResponse.builder()
                .summary(StudentManagementResponse.Summary.builder()
                        .totalStudents(studentRepository.countTotalStudents(branchId))
                        .activeStudents(studentRepository.countActiveStudents(branchId))
                        .inactiveStudents(studentRepository.countInactiveStudents(branchId))
                        .totalBranches(branchRepository.countByIsDeletedFalseAndOptionalStatus(null))
                        .activeBranches(branchRepository.countByIsDeletedFalseAndOptionalStatus(true))
                        .build())
                .content(studentPage.getContent().stream().map(this::toResponse).toList())
                .page(StudentManagementResponse.PageMeta.builder()
                        .pageNumber(studentPage.getNumber())
                        .pageSize(studentPage.getSize())
                        .totalElements(studentPage.getTotalElements())
                        .totalPages(studentPage.getTotalPages())
                        .first(studentPage.isFirst())
                        .last(studentPage.isLast())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(UUID studentId) {
        return toResponse(loadStudent(studentId));
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request, UUID createdByUserId) {
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());

        String normalizedStudentId = normalizeRequired(request.getStudentId(), "Student ID is required").toUpperCase();
        String normalizedLoginId = normalizeRequired(request.getLoginId(), "Login ID is required");
        String normalizedEmail = normalizeOptionalEmail(request.getEmail());

        validateUniqueStudentId(normalizedStudentId, null);
        validateUniqueLoginIdForCreate(normalizedLoginId);
        validateUniqueEmailForCreate(normalizedEmail);

        Branch branch = loadBranch(request.getBranchId());
        Role studentRole = loadStudentRole();
        User creator = loadUser(createdByUserId);

        User user = User.builder()
                .email(normalizedEmail)
                .loginId(normalizedLoginId)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getMobile().trim())
                .profilePhotoUrl(trimToNull(request.getProfilePhotoUrl()))
                .branch(branch)
                .isActive(true)
                .roles(Set.of(studentRole))
                .build();
        user = userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .branch(branch)
                .name(user.getFullName())
                .studentId(normalizedStudentId)
                .dob(request.getDateOfBirth())
                .gender(trimToNull(request.getGender()))
                .photoUrl(user.getProfilePhotoUrl())
                .mobile(user.getPhone())
                .parentName(request.getParentName().trim())
                .parentPhone(request.getParentPhone().trim())
                .email(normalizedEmail)
                .address(trimToNull(request.getAddress()))
                .schoolName(trimToNull(request.getSchoolName()))
                .standard(request.getStandard().trim())
                .batch(request.getBatch().trim())
                .board(parseBoard(request.getBoard()))
                .admissionDate(request.getAdmissionDate())
                .isAdmissionFinal(true)
                .isActive(true)
                .createdByUser(creator)
                .build();
        student = studentRepository.save(student);

        recordStudentActivity("STUDENT_CREATED", student, createdByUserId, "Student account created");
        return toResponse(student);
    }

    @Transactional
    public StudentResponse updateStudent(UUID studentId, UpdateStudentRequest request, UUID updatedByUserId) {
        Student student = loadStudent(studentId);
        User user = student.getUser();
        if (user == null) {
            throw new BadRequestException("Student does not have a linked login account", "STUDENT_USER_MISSING");
        }

        String normalizedStudentId = normalizeRequired(request.getStudentId(), "Student ID is required").toUpperCase();
        String normalizedLoginId = normalizeRequired(request.getLoginId(), "Login ID is required");
        String normalizedEmail = normalizeOptionalEmail(request.getEmail());

        validateUniqueStudentId(normalizedStudentId, student.getId());
        validateUniqueLoginIdForUpdate(user.getId(), normalizedLoginId);
        validateUniqueEmailForUpdate(user.getId(), normalizedEmail);
        handleOptionalPasswordUpdate(user, request.getPassword(), request.getConfirmPassword());

        Branch branch = loadBranch(request.getBranchId());

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(request.getMobile().trim());
        user.setLoginId(normalizedLoginId);
        user.setProfilePhotoUrl(trimToNull(request.getProfilePhotoUrl()));
        user.setBranch(branch);
        userRepository.save(user);

        student.setBranch(branch);
        student.setName(user.getFullName());
        student.setStudentId(normalizedStudentId);
        student.setDob(request.getDateOfBirth());
        student.setGender(trimToNull(request.getGender()));
        student.setPhotoUrl(user.getProfilePhotoUrl());
        student.setMobile(user.getPhone());
        student.setParentName(request.getParentName().trim());
        student.setParentPhone(request.getParentPhone().trim());
        student.setEmail(normalizedEmail);
        student.setAddress(trimToNull(request.getAddress()));
        student.setSchoolName(trimToNull(request.getSchoolName()));
        student.setStandard(request.getStandard().trim());
        student.setBatch(request.getBatch().trim());
        student.setBoard(parseBoard(request.getBoard()));
        student.setAdmissionDate(request.getAdmissionDate());
        student = studentRepository.save(student);

        recordStudentActivity("STUDENT_UPDATED", student, updatedByUserId, "Student profile updated");
        return toResponse(student);
    }

    @Transactional
    public StudentResponse updateStudentStatus(UUID studentId, UpdateStudentStatusRequest request, UUID updatedByUserId) {
        Student student = loadStudent(studentId);
        User user = student.getUser();

        student.setIsActive(request.getIsActive());
        studentRepository.save(student);

        if (user != null) {
            user.setIsActive(request.getIsActive());
            userRepository.save(user);

            if (!Boolean.TRUE.equals(request.getIsActive())) {
                refreshTokenRepository.revokeAllForUser(user.getId(), LocalDateTime.now(), "STUDENT_DEACTIVATED");
            }
        }

        String description = Boolean.TRUE.equals(request.getIsActive())
                ? "Student account activated"
                : "Student account deactivated" + (StringUtils.hasText(request.getReason()) ? ": " + request.getReason().trim() : "");
        recordStudentActivity(Boolean.TRUE.equals(request.getIsActive()) ? "STUDENT_ACTIVATED" : "STUDENT_DEACTIVATED",
                student, updatedByUserId, description);
        return toResponse(student);
    }

    @Transactional
    public void deleteStudent(UUID studentId, UUID deletedByUserId) {
        Student student = loadStudent(studentId);
        User user = student.getUser();

        student.softDelete();
        student.setIsActive(false);
        studentRepository.save(student);

        if (user != null) {
            user.softDelete();
            user.setIsActive(false);
            userRepository.save(user);
            refreshTokenRepository.revokeAllForUser(user.getId(), LocalDateTime.now(), "STUDENT_DELETED");
        }

        recordStudentActivity("STUDENT_DELETED", student, deletedByUserId, "Student account soft deleted");
    }

    private Student loadStudent(UUID studentId) {
        return studentRepository.findActiveById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
    }

    private Branch loadBranch(UUID branchId) {
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private User loadUser(UUID userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Role loadStudentRole() {
        return roleRepository.findByName(UserRole.STUDENT.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role", UserRole.STUDENT.name()));
    }

    private StudentResponse toResponse(Student student) {
        User user = student.getUser();
        Branch branch = student.getBranch();

        return StudentResponse.builder()
                .id(student.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(student.getName())
                .studentId(student.getStudentId())
                .branchId(branch.getId())
                .branchName(branch.getName())
                .standard(student.getStandard())
                .batch(student.getBatch())
                .gender(student.getGender())
                .dateOfBirth(student.getDob())
                .mobile(student.getMobile())
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .email(student.getEmail())
                .address(student.getAddress())
                .schoolName(student.getSchoolName())
                .board(student.getBoard())
                .admissionDate(student.getAdmissionDate())
                .loginId(user != null ? user.getLoginId() : null)
                .profilePhotoUrl(user != null ? user.getProfilePhotoUrl() : student.getPhotoUrl())
                .isAdmissionFinal(student.getIsAdmissionFinal())
                .isActive(student.getIsActive())
                .lastLoginAt(user != null ? user.getLastLoginAt() : null)
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    private BoardType parseBoard(String board) {
        try {
            return BoardType.valueOf(board.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new BadRequestException("Board must be one of SSC, CBSE, or ICSE", "INVALID_BOARD");
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
        refreshTokenRepository.revokeAllForUser(user.getId(), LocalDateTime.now(), "PASSWORD_CHANGED_BY_STUDENT_MANAGER");
    }

    private void validateUniqueStudentId(String studentId, UUID currentStudentId) {
        studentRepository.findByStudentIdIgnoreCaseAndIsDeletedFalse(studentId)
                .filter(existing -> currentStudentId == null || !existing.getId().equals(currentStudentId))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("A student with this student ID already exists.");
                });
    }

    private void validateUniqueLoginIdForCreate(String loginId) {
        if (userRepository.existsByLoginIdIgnoreCaseAndIsDeletedFalse(loginId)) {
            throw new ResourceAlreadyExistsException("A user with this login ID already exists.");
        }
    }

    private void validateUniqueEmailForCreate(String email) {
        if (email != null && userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(email)) {
            throw new ResourceAlreadyExistsException("A user with this email already exists.");
        }
    }

    private void validateUniqueEmailForUpdate(UUID userId, String email) {
        if (email == null) {
            return;
        }
        userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("A user with this email already exists.");
                });
    }

    private void validateUniqueLoginIdForUpdate(UUID userId, String loginId) {
        userRepository.findByLoginIdIgnoreCaseAndIsDeletedFalse(loginId)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("A user with this login ID already exists.");
                });
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String normalizeOptionalEmail(String email) {
        return normalize(email);
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

    private void recordStudentActivity(String type, Student student, UUID actorUserId, String description) {
        OperationalRecord record = OperationalRecord.builder()
                .module(MODULE_ACTIVITY)
                .type(type)
                .title(student.getName())
                .description(description)
                .branch(student.getBranch())
                .studentId(student.getId())
                .createdByUserId(actorUserId)
                .eventDate(LocalDate.now())
                .status("COMPLETED")
                .build();
        operationalRecordRepository.save(record);
    }
}
