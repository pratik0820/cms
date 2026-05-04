package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.RoleRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import com.classmanager.cms_backend.tenant.TenantContext;
import com.classmanager.cms_backend.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService extends BaseService {

    private static final Logger log = LogManager.getLogger(UserService.class);
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private static final Random RANDOM = new Random();

    public User getUserById(UUID userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Transactional
    public User createUser(String email, String password, String fullName,
                           String phone, UserRole role, UUID branchId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (userRepository.existsByEmailAndTenantIdAndIsDeletedFalse(email, tenantId)) {
            throw new ResourceAlreadyExistsException(
                    "A user with email '" + email + "' already exists.");
        }

        Role dbRole = roleRepository.findByName(role.name())
                .orElseThrow(() -> new RuntimeException("Role not found: " + role.name()));

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .phone(phone)
                .roles(Set.of(dbRole))
                .isActive(true)
                .build();
        user.setTenantId(tenantId);

        if (branchId != null) {
            var branch = branchRepository.findByIdAndIsDeletedFalse(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
            user.setBranch(branch);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", userId);
    }

    @Transactional
    public void activateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void updateFcmToken(UUID userId, String fcmToken) {
        userRepository.updateFcmToken(userId, fcmToken);
    }

    public String generateStudentLoginId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("STU-");

        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }

        return sb.toString();
    }

    public String generateTemporaryPassword() {
        return PasswordUtils.generateDefault();
    }

    public Optional<User> findByUserEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUserPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
}
