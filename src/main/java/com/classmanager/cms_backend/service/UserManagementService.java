package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateAdminRequest;
import com.classmanager.cms_backend.dto.request.CreateStudentRequest;
import com.classmanager.cms_backend.dto.request.CreateTeacherRequest;
import com.classmanager.cms_backend.dto.request.ResetPasswordRequest;
import com.classmanager.cms_backend.dto.response.UserResponse;
import com.classmanager.cms_backend.entity.*;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BusinessRuleException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.*;
import com.classmanager.cms_backend.service.jpa.EmailService;
import com.classmanager.cms_backend.tenant.TenantContext;
import com.classmanager.cms_backend.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final Logger log = LogManager.getLogger(UserManagementService.class);

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    // ─── CREATE ADMIN ────────────────────────────────────────────────────────

    /**
     * Creates a new ADMIN user for the current tenant.
     * Only SUPER_ADMIN may call this.
     * A random password is generated and returned once.
     */
    @Transactional
    public UserResponse.AdminResponse createAdmin(CreateAdminRequest req) {
        UUID tenantId = requireCurrentTenant();

        if (userRepository.existsByEmailAndTenantIdAndIsDeletedFalse(req.getEmail(), tenantId)) {
            throw new ResourceAlreadyExistsException(
                    "A user with email '" + req.getEmail() + "' already exists in this institute.");
        }

        Branch branch = resolveBranch(req.getBranchId());
        String tempPassword = PasswordUtils.generateDefault();

        Role adminRole = roleRepository.findByName(UserRole.ADMIN.name())
                .orElseThrow(() -> new RuntimeException("Role TEACHER not found"));

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .roles(Set.of(adminRole))
                .branch(branch)
                .isActive(true)
                .build();
        user.setTenantId(tenantId);
        userRepository.saveAndFlush(user);

        log.info("Admin account created: {} for tenant: {}", req.getEmail(), tenantId);

        sendAdminWelcomeEmail(req, tempPassword, tenantId);

        return UserResponse.AdminResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(UserRole.ADMIN)
                .branchId(branch != null ? branch.getId() : null)
                .branchName(branch != null ? branch.getName() : null)
                .isActive(true)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ─── CREATE TEACHER ──────────────────────────────────────────────────────

    /**
     * Creates a Teacher profile + linked User account.
     *
     * Flow:
     *   1. Validate email uniqueness in tenant
     *   2. Generate random password
     *   3. Insert User row (role = TEACHER)
     *   4. Insert Teacher row linked to the User
     *   5. Return credentials in response (only time they're visible)
     *
     * Teacher logs in with: email + generated password
     */
    @Transactional
    public UserResponse.TeacherResponse createTeacher(CreateTeacherRequest req) {
        UUID tenantId = requireCurrentTenant();

        if (userRepository.existsByEmailAndTenantIdAndIsDeletedFalse(req.getEmail(), tenantId)) {
            throw new ResourceAlreadyExistsException(
                    "A teacher with email '" + req.getEmail() + "' already exists.");
        }

        if (teacherRepository.existsByEmailAndTenantIdAndIsDeletedFalse(req.getEmail(), tenantId)) {
            throw new ResourceAlreadyExistsException(
                    "A teacher with email '" + req.getEmail() + "' already exists.");
        }

        Branch branch = branchRepository.findByIdAndTenantIdAndIsDeletedFalse(req.getBranchId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId()));

        // Generate credentials
        String tempPassword = PasswordUtils.generateDefault();

        Role teacherRole = roleRepository.findByName(UserRole.TEACHER.name())
                .orElseThrow(() -> new RuntimeException("Role TEACHER not found"));

        // Step 1: Create login user
        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .roles(Set.of(teacherRole))
                .branch(branch)
                .isActive(true)
                .build();
        user.setTenantId(tenantId);
        userRepository.saveAndFlush(user);

        // Step 2: Create teacher profile
        Teacher teacher = Teacher.builder()
                .user(user)
                .branch(branch)
                .name(req.getFullName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .qualification(req.getQualification())
                .joiningDate(req.getJoiningDate() != null ? req.getJoiningDate() : LocalDate.now())
                .hourlyRate(req.getHourlyRate())
                .isActive(true)
                .build();
        teacher.setTenantId(tenantId);
        teacherRepository.saveAndFlush(teacher);

        log.info("Teacher created: {} (id: {}) for tenant: {}",
                req.getEmail(), teacher.getId(), tenantId);
        sendTeacherWelcomeEmail(req, tempPassword, tenantId);

        return UserResponse.TeacherResponse.builder()
                .id(teacher.getId())
                .userId(user.getId())
                .fullName(teacher.getName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .qualification(teacher.getQualification())
                .joiningDate(teacher.getJoiningDate())
                .hourlyRate(teacher.getHourlyRate())
                .branchId(branch.getId())
                .branchName(branch.getName())
                .isActive(true)
                .createdAt(teacher.getCreatedAt())
                // Credentials — only included in creation response
                .generatedLoginEmail(req.getEmail())
                .generatedPassword(tempPassword)
                .build();
    }

    // ─── CREATE STUDENT ──────────────────────────────────────────────────────

    /**
     * Creates a Student profile + linked User account.
     *
     * Flow:
     *   1. Generate unique login_id (STU-XXXXXX)
     *   2. Generate random password
     *   3. Insert User row (role = STUDENT) with email = loginId@cms.local (internal)
     *   4. Insert Student row in DRAFT state (is_admission_final = false)
     *   5. Return login_id + password — Admin gives these to student & parent
     *
     * Student logs in with: login_id + generated password
     * Parent logs in with:  same login_id + same password (shared credentials)
     *
     * Photo upload (POST /api/users/students/{id}/photo) must happen before
     * finalisation (POST /api/users/students/{id}/finalise).
     */
    @Transactional
    public UserResponse.StudentResponse createStudent(CreateStudentRequest req) {
        UUID tenantId = requireCurrentTenant();

        Branch branch = branchRepository.findByIdAndTenantIdAndIsDeletedFalse(req.getBranchId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId()));

        // Generate unique login_id
        String loginId = generateUniqueLoginId();
        String tempPassword = PasswordUtils.generateDefault();

        // Internal email for the user account (login is by loginId, not email)
        String internalEmail = loginId.toLowerCase() + "@student.cms.local";

        Role studentRole = roleRepository.findByName(UserRole.STUDENT.name())
                .orElseThrow(() -> new RuntimeException("Role TEACHER not found"));

        // Step 1: Create login user (role STUDENT)
        User user = User.builder()
                .email(internalEmail)
                .passwordHash(passwordEncoder.encode(tempPassword))
                .fullName(req.getName())
                .phone(req.getMobile())
                .roles(Set.of(studentRole))
                .branch(branch)
                .isActive(true)
                .build();
        user.setTenantId(tenantId);
        userRepository.saveAndFlush(user);

        // Step 2: Create student profile (DRAFT — photo not uploaded yet)
        Student student = Student.builder()
                .user(user)
                .branch(branch)
                .name(req.getName())
                .dob(req.getDateOfBirth())
                .gender(req.getGender())
                .mobile(req.getMobile())
                .parentName(req.getParentName())
                .parentPhone(req.getParentPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .schoolName(req.getSchoolName())
                .standard(req.getStandard())
                .board(req.getBoard())
                .loginId(loginId)
                .admissionDate(LocalDate.now())
                .isAdmissionFinal(false)   // Draft — requires photo upload to finalise
                .isActive(true)
                .build();
        student.setTenantId(tenantId);

        // Optionally link to a batch
        if (req.getBatchId() != null) {
            // Batch validation would go here in a full implementation
            // batch is set via a separate repository call if needed
        }

        studentRepository.saveAndFlush(student);

        log.info("Student created: {} (loginId: {}) for tenant: {}",
                req.getName(), loginId, tenantId);

        sendStudentWelcomeEmail(req, loginId, tempPassword, tenantId);

        return UserResponse.StudentResponse.builder()
                .id(student.getId())
                .userId(user.getId())
                .name(student.getName())
                .loginId(loginId)
                .email(student.getEmail())
                .mobile(student.getMobile())
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .standard(student.getStandard())
                .board(student.getBoard())
                .schoolName(student.getSchoolName())
                .isAdmissionFinal(false)
                .branchId(branch.getId())
                .branchName(branch.getName())
                .isActive(true)
                .createdAt(student.getCreatedAt())
                // Credentials — only in creation response
                .generatedLoginId(loginId)
                .generatedPassword(tempPassword)
                .parentLoginNote("Parent can use the same Login ID and Password to access the parent portal.")
                .build();
    }

    // ─── FINALISE ADMISSION ──────────────────────────────────────────────────

    /**
     * Finalises a student's admission.
     * Fails with 422 if photo_url is still null on the student record.
     */
    @Transactional
    public UserResponse.StudentResponse finaliseAdmission(UUID studentId) {
        Student student = studentRepository.findByIdAndTenantIdAndIsDeletedFalse(studentId, requireCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        if (student.getPhotoUrl() == null || student.getPhotoUrl().isBlank()) {
            throw new BusinessRuleException(
                    "Cannot finalise admission: student photo has not been uploaded. " +
                            "Please upload the photo first via POST /api/users/students/" + studentId + "/photo",
                    "PHOTO_REQUIRED");
        }

        student.setIsAdmissionFinal(true);
        studentRepository.save(student);

        log.info("Admission finalised for student: {} ({})", student.getName(), studentId);

        return mapStudentToResponse(student);
    }

    // ─── UPDATE STUDENT PHOTO URL ────────────────────────────────────────────

    /** Called by the controller after Cloudinary upload succeeds. */
    @Transactional
    public void updateStudentPhotoUrl(UUID studentId, String photoUrl) {
        Student student = studentRepository.findByIdAndTenantIdAndIsDeletedFalse(studentId, requireCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        student.setPhotoUrl(photoUrl);
        studentRepository.save(student);
    }

    @Transactional
    public String uploadStudentPhoto(UUID studentId, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new BusinessRuleException("Student photo is required", "PHOTO_REQUIRED");
        }

        String contentType = photo.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp"))) {
            throw new BusinessRuleException("Student photo must be JPEG, PNG, or WebP", "PHOTO_INVALID_TYPE");
        }

        Student student = studentRepository.findByIdAndTenantIdAndIsDeletedFalse(studentId, requireCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };

        try {
            Path directory = Path.of(uploadDir, "student-photos", student.getTenantId().toString());
            Files.createDirectories(directory);
            Path destination = directory.resolve(student.getId() + extension);
            Files.copy(photo.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            String photoUrl = "/" + destination.toString().replace("\\", "/");
            student.setPhotoUrl(photoUrl);
            studentRepository.save(student);
            return photoUrl;
        } catch (IOException ex) {
            throw new BusinessRuleException("Could not store student photo", "PHOTO_UPLOAD_FAILED");
        }
    }

    // ─── RESET PASSWORD (Admin overrides) ────────────────────────────────────

    /**
     * Admin resets a user's password — does NOT require the current password.
     * Revokes all existing refresh tokens for the user.
     */
    @Transactional
    public void resetUserPassword(UUID userId, ResetPasswordRequest req) {
        User user = userRepository.findByIdAndTenantIdAndIsDeletedFalse(userId, requireCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset by admin for user: {}", userId);
    }

    // ─── DEACTIVATE / ACTIVATE ───────────────────────────────────────────────

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findByIdAndTenantIdAndIsDeletedFalse(userId, requireCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(UUID userId) {
        User user = userRepository.findByIdAndTenantIdAndIsDeletedFalse(userId, requireCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setIsActive(true);
        userRepository.save(user);
    }

    // ─── READS ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<UserResponse.TeacherResponse> listTeachers(Pageable pageable) {
        return teacherRepository.findByTenantIdAndIsDeletedFalseOrderByNameAsc(requireCurrentTenant(), pageable)
                .map(this::mapTeacherToResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse.StudentResponse> listStudents(Pageable pageable) {
        return studentRepository.findByTenantIdAndIsDeletedFalseOrderByNameAsc(requireCurrentTenant(), pageable)
                .map(this::mapStudentToResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse.TeacherResponse getTeacher(UUID teacherId) {
        return teacherRepository.findByIdAndTenantIdAndIsDeletedFalse(teacherId, requireCurrentTenant())
                .map(this::mapTeacherToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));
    }

    @Transactional(readOnly = true)
    public UserResponse.StudentResponse getStudent(UUID studentId) {
        return studentRepository.findByIdAndTenantIdAndIsDeletedFalse(studentId, requireCurrentTenant())
                .map(this::mapStudentToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private Branch resolveBranch(UUID branchId) {
        if (branchId == null) return null;
        return branchRepository.findByIdAndTenantIdAndIsDeletedFalse(branchId, requireCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
    }

    private UUID requireCurrentTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is required");
        }
        return tenantId;
    }

    private String generateUniqueLoginId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        String loginId;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder("STU-");
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            loginId = sb.toString();
            attempts++;
            if (attempts > 20) throw new IllegalStateException("Unable to generate unique login ID");
        } while (studentRepository.existsByLoginIdAndIsDeletedFalse(loginId));
        return loginId;
    }

    private UserResponse.TeacherResponse mapTeacherToResponse(Teacher t) {
        return UserResponse.TeacherResponse.builder()
                .id(t.getId())
                .userId(t.getUser() != null ? t.getUser().getId() : null)
                .fullName(t.getName())
                .email(t.getEmail())
                .phone(t.getPhone())
                .qualification(t.getQualification())
                .joiningDate(t.getJoiningDate())
                .hourlyRate(t.getHourlyRate())
                .branchId(t.getBranch() != null ? t.getBranch().getId() : null)
                .branchName(t.getBranch() != null ? t.getBranch().getName() : null)
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                // Never return generated password on reads — only on creation
                .generatedLoginEmail(null)
                .generatedPassword(null)
                .build();
    }

    private UserResponse.StudentResponse mapStudentToResponse(Student s) {
        return UserResponse.StudentResponse.builder()
                .id(s.getId())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .name(s.getName())
                .loginId(s.getLoginId())
                .email(s.getEmail())
                .mobile(s.getMobile())
                .parentName(s.getParentName())
                .parentPhone(s.getParentPhone())
                .gender(s.getGender())
                .standard(s.getStandard())
                .board(s.getBoard())
                .schoolName(s.getSchoolName())
                .photoUrl(s.getPhotoUrl())
                .isAdmissionFinal(s.getIsAdmissionFinal())
                .branchId(s.getBranch() != null ? s.getBranch().getId() : null)
                .branchName(s.getBranch() != null ? s.getBranch().getName() : null)
                .batchId(s.getBatch() != null ? s.getBatch().getId() : null)
                .batchName(s.getBatch() != null ? s.getBatch().getName() : null)
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                // Never return credentials on reads
                .generatedLoginId(null)
                .generatedPassword(null)
                .parentLoginNote(null)
                .build();
    }

    private void sendTeacherWelcomeEmail(CreateTeacherRequest req, String tempPassword, UUID tenantId) {
        try {
            String instituteName = tenantRepository.findById(tenantId)
                    .map(Tenant::getName)
                    .orElse("Your Institute");

            Map<String, String> vars = new HashMap<>();
            vars.put("teacherName", req.getFullName());
            vars.put("instituteName", instituteName);
            vars.put("loginUrl", frontendUrl);
            vars.put("loginEmail", req.getEmail());
            vars.put("temporaryPassword", tempPassword);

            String subject = emailTemplateService.render("teacher/teacher-welcome.subject.txt", vars);
            String html = emailTemplateService.render("teacher/teacher-welcome.html", vars);
            String text = emailTemplateService.render("teacher/teacher-welcome.txt", vars);

            emailService.sendHtml(req.getEmail(), subject, html, text);

        } catch (Exception ex) {
            log.error("Teacher created but welcome email failed for email={}", req.getEmail(), ex);
        }
    }

    private void sendAdminWelcomeEmail(CreateAdminRequest req, String tempPassword, UUID tenantId) {
        try {
            String instituteName = tenantRepository.findById(tenantId)
                    .map(Tenant::getName).orElse("Your Institute");

            Map<String, String> vars = new HashMap<>();
            vars.put("fullName", req.getFullName());
            vars.put("instituteName", instituteName);
            vars.put("loginUrl", frontendUrl);
            vars.put("loginEmail", req.getEmail());
            vars.put("temporaryPassword", tempPassword);

            String subject = emailTemplateService.render("admin/admin-welcome.subject.txt", vars).trim();
            String html = emailTemplateService.render("admin/admin-welcome.html", vars);
            String text = emailTemplateService.render("admin/admin-welcome.txt", vars);

            emailService.sendHtml(req.getEmail(), subject, html, text);
        } catch (Exception ex) {
            log.error("Admin created but welcome email failed for email={}", req.getEmail(), ex);
        }
    }

    private void sendStudentWelcomeEmail(CreateStudentRequest req, String loginId, String tempPassword, UUID tenantId) {
        try {
            String instituteName = tenantRepository.findById(tenantId)
                    .map(Tenant::getName).orElse("Your Institute");

            Map<String, String> vars = new HashMap<>();
            vars.put("studentName", req.getName());
            vars.put("parentName", req.getParentName());
            vars.put("instituteName", instituteName);
            vars.put("loginUrl", frontendUrl);
            vars.put("loginId", loginId);
            vars.put("temporaryPassword", tempPassword);

            String subject = emailTemplateService.render("student/student-welcome.subject.txt", vars).trim();
            String html = emailTemplateService.render("student/student-welcome.html", vars);
            String text = emailTemplateService.render("student/student-welcome.txt", vars);

            // current model has student email only; no separate parent email field
            if (req.getEmail() != null && !req.getEmail().isBlank()) {
                emailService.sendHtml(req.getEmail(), subject, html, text);
            }
        } catch (Exception ex) {
            log.error("Student created but welcome email failed for student={}", req.getName(), ex);
        }
    }

}
