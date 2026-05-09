package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.BootstrapSuperAdminRequest;
import com.classmanager.cms_backend.dto.request.ChangePasswordRequest;
import com.classmanager.cms_backend.dto.request.LoginRequest;
import com.classmanager.cms_backend.dto.request.RefreshTokenRequest;
import com.classmanager.cms_backend.dto.response.AuthResponse;
import com.classmanager.cms_backend.entity.RefreshToken;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.exception.UnauthorizedException;
import com.classmanager.cms_backend.repository.RefreshTokenRepository;
import com.classmanager.cms_backend.repository.RoleRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.security.jwt.JwtProperties;
import com.classmanager.cms_backend.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LogManager.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${rate-limit.login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${rate-limit.login-window-minutes:15}")
    private int loginLockMinutes;

    @Transactional
    public AuthResponse bootstrapSuperAdmin(BootstrapSuperAdminRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByRoleName(UserRole.SUPER_ADMIN.name())) {
            throw new ResourceAlreadyExistsException("A super admin account already exists.");
        }
        ensurePasswordConfirmation(request.getPassword(), request.getConfirmPassword());

        String normalizedEmail = normalizeIdentifier(request.getEmail());
        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(normalizedEmail)) {
            throw new ResourceAlreadyExistsException("A user with this email already exists.");
        }

        Role superAdminRole = roleRepository.findByName(UserRole.SUPER_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role", UserRole.SUPER_ADMIN.name()));

        User user = User.builder()
                .email(normalizedEmail)
                .fullName(request.getFullName().trim())
                .phone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(superAdminRole))
                .isActive(true)
                .build();

        user.recordSuccessfulLogin();
        user = userRepository.save(user);

        String accessToken = generateAccessToken(user);
        String rawRefreshToken = persistRefreshToken(user, httpRequest);

        log.info("Bootstrap super admin created with userId={}", user.getId());
        return buildAuthResponse(user, accessToken, rawRefreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String loginIdentifier = normalizeIdentifier(request.getLoginIdentifier());
        if (!StringUtils.hasText(loginIdentifier)) {
            throw new UnauthorizedException("Email or login ID is required", "AUTH_IDENTIFIER_REQUIRED");
        }

        User user = findLoginUser(loginIdentifier);

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is disabled. Contact admin.", "AUTH_ACCOUNT_DISABLED");
        }
        if (user.isAccountLocked()) {
            throw new UnauthorizedException(
                    "Account temporarily locked. Try again after " + user.getLockedUntil(),
                    "AUTH_ACCOUNT_LOCKED"
            );
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginIdentifier, request.getPassword())
            );

            CmsUserDetails userDetails = (CmsUserDetails) auth.getPrincipal();
            user = userDetails.getUser();
            user.recordSuccessfulLogin();
        } catch (Exception ex) {
            user.recordFailedLogin(maxLoginAttempts, loginLockMinutes);
            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password", "AUTH_INVALID_CREDENTIALS");
        }

        if (StringUtils.hasText(request.getFcmToken())) {
            user.setFcmToken(request.getFcmToken().trim());
        }
        userRepository.save(user);

        String accessToken = generateAccessToken(user);
        String rawRefreshToken = persistRefreshToken(user, httpRequest);

        log.info("Login successful for userId={} roles={}", user.getId(), user.getRoles());
        return buildAuthResponse(user, accessToken, rawRefreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String hashedToken = hashToken(request.getRefreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new UnauthorizedException(
                        "Invalid or expired refresh token", "AUTH_INVALID_REFRESH_TOKEN"));

        if (storedToken.isUsed()) {
            refreshTokenRepository.revokeAllForUser(
                    storedToken.getUser().getId(), LocalDateTime.now(), "REUSE_DETECTED");
            throw new UnauthorizedException(
                    "Security violation detected. Please log in again.", "AUTH_TOKEN_REUSE");
        }

        if (!storedToken.isValid()) {
            throw new UnauthorizedException(
                    "Refresh token has expired or been revoked. Please log in again.",
                    "AUTH_REFRESH_TOKEN_EXPIRED");
        }

        User user = storedToken.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is disabled.", "AUTH_ACCOUNT_DISABLED");
        }

        storedToken.markUsed();
        refreshTokenRepository.save(storedToken);

        String accessToken = generateAccessToken(user);
        String rawRefreshToken = persistRefreshToken(user, httpRequest);
        return buildAuthResponse(user, accessToken, rawRefreshToken);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String hashedToken = hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(hashedToken)
                .ifPresent(token -> {
                    token.revoke("USER_LOGOUT");
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void logoutAllDevices(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId, LocalDateTime.now(), "LOGOUT_ALL_DEVICES");
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        ensurePasswordConfirmation(request.getNewPassword(), request.getConfirmPassword());

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect", "WRONG_CURRENT_PASSWORD");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(userId, LocalDateTime.now(), "PASSWORD_CHANGED");
    }

    public AuthResponse.UserInfo buildUserInfo(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .loginId(user.getLoginId())
                .fullName(user.getFullName())
                .roles(roles)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .build();
    }

    private User findLoginUser(String loginIdentifier) {
        return userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(loginIdentifier)
                .or(() -> userRepository.findByLoginIdIgnoreCaseAndIsDeletedFalse(loginIdentifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password", "AUTH_INVALID_CREDENTIALS"));
    }

    private String generateAccessToken(User user) {
        UUID branchId = user.getBranch() != null ? user.getBranch().getId() : null;
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        return jwtService.generateAccessToken(user.getId(), roles, branchId, user.getEmail(), user.getLoginId());
    }

    private String persistRefreshToken(User user, HttpServletRequest httpRequest) {
        String rawRefreshToken = generateSecureToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiryMs() / 1000))
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawRefreshToken;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String rawRefreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpiryMs() / 1000)
                .user(buildUserInfo(user))
                .build();
    }

    private void ensurePasswordConfirmation(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BadRequestException(
                    "Password and confirm password do not match",
                    "PASSWORD_MISMATCH"
            );
        }
    }

    private String normalizeIdentifier(String identifier) {
        return identifier == null ? null : identifier.trim().toLowerCase();
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BadRequestException("SHA-256 not available", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
