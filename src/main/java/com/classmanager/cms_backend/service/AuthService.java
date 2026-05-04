package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.ChangePasswordRequest;
import com.classmanager.cms_backend.dto.request.LoginRequest;
import com.classmanager.cms_backend.dto.request.RefreshTokenRequest;
import com.classmanager.cms_backend.dto.response.AuthResponse;
import com.classmanager.cms_backend.entity.RefreshToken;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.entity.Tenant;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.exception.UnauthorizedException;
import com.classmanager.cms_backend.repository.RefreshTokenRepository;
import com.classmanager.cms_backend.repository.TenantRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.security.jwt.JwtProperties;
import com.classmanager.cms_backend.security.jwt.JwtService;
import com.classmanager.cms_backend.tenant.TenantContext;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService extends BaseService {

    private static final Logger log = LogManager.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${rate-limit.login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${rate-limit.login-window-minutes:15}")
    private int loginLockMinutes;


    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {

        String identifier = request.getLoginIdentifier();
        if (!StringUtils.hasText(identifier)) {
            throw new UnauthorizedException("Email or login ID is required", "AUTH_IDENTIFIER_REQUIRED");
        }
        identifier = identifier.toLowerCase().trim();
        String loginIdentifier = identifier;
        UUID requestedTenantId = resolveRequestedTenantId(request);

        log.info("Login attempt for identifier={} tenantId={}", loginIdentifier, requestedTenantId);

        // Step 1: Find user by staff email or generated student loginId.
        User user = userRepository.findByEmailAndTenantIdAndIsDeletedFalse(loginIdentifier, requestedTenantId)
                .or(() -> userRepository.findStudentUserByLoginIdAndTenantId(loginIdentifier, requestedTenantId))
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found or tenant mismatch. identifier={} tenantId={}",
                            loginIdentifier, requestedTenantId);
                    return new UnauthorizedException(
                            "Invalid email/login ID, password, or tenant",
                            "AUTH_INVALID_CREDENTIALS"
                    );
                });

        log.debug("User found | userId={} email={}", user.getId(), user.getEmail());

        // Step 1.1  Validate tenant explicitly
        if (!user.getTenantId().equals(requestedTenantId)) {
            log.error("Tenant mismatch during login! identifier={} tokenTenant={} actualTenant={}",
                    loginIdentifier, requestedTenantId, user.getTenantId());
            throw new UnauthorizedException("Invalid tenant access", "AUTH_TENANT_MISMATCH");
        }

        // Step 2: Check account status
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Login blocked - account disabled | email={}", user.getEmail());
            throw new UnauthorizedException("Account is disabled. Contact admin.", "AUTH_ACCOUNT_DISABLED");
        }
        if (user.isAccountLocked()) {
            log.warn("Login blocked - account locked | email={} lockedUntil={}",
                    user.getEmail(), user.getLockedUntil());
            throw new UnauthorizedException(
                    "Account temporarily locked. Try again after " + user.getLockedUntil(),
                    "AUTH_ACCOUNT_LOCKED");
        }

        // Step 3: Authenticate (validates BCrypt password)
        try {
            log.debug("Authenticating user | identifier={}", loginIdentifier);
            TenantContext.setCurrentTenant(requestedTenantId);

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(), request.getPassword()
                    )
            );

            CmsUserDetails userDetails = (CmsUserDetails) auth.getPrincipal();

            if (!userDetails.getTenantId().equals(requestedTenantId)) {
                log.error("Post-auth tenant mismatch | email={} tokenTenant={} actualTenant={}",
                        user.getEmail(), requestedTenantId, userDetails.getTenantId());
                throw new SecurityException("Tenant mismatch after authentication");
            }

            user.recordSuccessfulLogin();
            log.info("Authentication successful | email={} userId={}", user.getEmail(), user.getId());

        } catch (Exception ex) {
            // Record failed attempt and potentially lock account
            user.recordFailedLogin(maxLoginAttempts, loginLockMinutes);
            userRepository.save(user);
            log.warn("Failed login attempt for identifier: {}", loginIdentifier);
            throw new UnauthorizedException("Invalid email or password", "AUTH_INVALID_CREDENTIALS");
        }

        // Step 4: Generate access token
        UUID branchId = user.getBranch() != null ? user.getBranch().getId() : null;

        log.debug("Generating access token | userId={} tenantId={}", user.getId(), user.getTenantId());

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getTenantId(), roles, branchId, user.getEmail()
        );

        log.debug("Generating refresh token | userId={}", user.getId());

        // Step 5: Generate & store refresh token
        String rawRefreshToken = generateSecureToken();
        String hashedRefreshToken = hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashedRefreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpiryMs() / 1000))
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();
        refreshTokenRepository.save(refreshToken);

        log.debug("Refresh token stored | userId={}", user.getId());

        // Step 6: Update FCM token
        if (StringUtils.hasText(request.getFcmToken())) {
            user.setFcmToken(request.getFcmToken());
            log.debug("FCM token updated | email={}", user.getEmail());
        }

        userRepository.save(user);

        // Step 7: Load tenant info for response
        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", user.getTenantId()));

        log.info("Successful login: {} | tenant: {} | role: {}",
                user.getEmail(), tenant.getName(), user.getRoles());

        return buildAuthResponse(user, tenant, accessToken, rawRefreshToken);
    }

    // ─── REFRESH TOKEN ───────────────────────────────────────────────────────

    /**
     * Exchanges a valid refresh token for a new access token + rotated refresh token.
     *
     * Refresh token rotation: the old refresh token is marked as used and a new one
     * is issued. If a token is reused (already marked used), all tokens for that user
     * are revoked (possible token theft detected).
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String hashedToken = hashToken(request.getRefreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new UnauthorizedException(
                        "Invalid or expired refresh token", "AUTH_INVALID_REFRESH_TOKEN"));

        // Detect token reuse — possible theft
        if (storedToken.isUsed()) {
            log.warn("Refresh token reuse detected for user: {}. Revoking all tokens.",
                    storedToken.getUser().getId());
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

        // Mark old token as used (rotation)
        storedToken.markUsed();
        refreshTokenRepository.save(storedToken);

        // Issue new access token
        UUID branchId = user.getBranch() != null ? user.getBranch().getId() : null;

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        String newAccessToken = jwtService.generateAccessToken(
                user.getId(), user.getTenantId(), roles, branchId, user.getEmail()
        );

        // Issue new refresh token
        String rawNewRefreshToken = generateSecureToken();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawNewRefreshToken))
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpiryMs() / 1000))
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", user.getTenantId()));

        return buildAuthResponse(user, tenant, newAccessToken, rawNewRefreshToken);
    }

    // ─── LOGOUT ──────────────────────────────────────────────────────────────

    /** Revokes the specific refresh token (logout from current device). */
    @Transactional
    public void logout(RefreshTokenRequest request) {
        String hashedToken = hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(hashedToken)
                .ifPresent(token -> token.revoke("USER_LOGOUT"));
    }

    /** Revokes ALL refresh tokens for the user (logout from all devices). */
    @Transactional
    public void logoutAllDevices(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId, LocalDateTime.now(), "LOGOUT_ALL_DEVICES");
        log.info("All refresh tokens revoked for user: {}", userId);
    }

    // ─── CHANGE PASSWORD ─────────────────────────────────────────────────────

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(
                    "New password and confirm password do not match", "PASSWORD_MISMATCH");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect", "WRONG_CURRENT_PASSWORD");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all other sessions after password change
        refreshTokenRepository.revokeAllForUser(userId, LocalDateTime.now(), "PASSWORD_CHANGED");
        log.info("Password changed for user: {}", userId);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, Tenant tenant,
                                           String accessToken, String rawRefreshToken) {

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpiryMs() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .roles(roles)
                        .tenantId(user.getTenantId())
                        .tenantName(tenant.getName())
                        .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                        .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                        .build())
                .build();
    }

    /** Generates a cryptographically secure 256-bit random token. */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /** SHA-256 hashes a token — stored in DB, never the raw token. */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BadRequestException("SHA-256 not available", e.getMessage());
        }
    }

    /** Extracts client IP, handles reverse proxies (X-Forwarded-For). */
    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private UUID resolveRequestedTenantId(LoginRequest request) {
        if (StringUtils.hasText(request.getTenantId())) {
            try {
                return UUID.fromString(request.getTenantId());
            } catch (Exception e) {
                throw new UnauthorizedException("Invalid credentials", "AUTH_INVALID_CREDENTIALS");
            }
        }

        if (StringUtils.hasText(request.getTenantSubdomain())) {
            return tenantRepository.findBySubdomainAndIsActiveTrue(request.getTenantSubdomain().trim().toLowerCase())
                    .map(Tenant::getId)
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials", "AUTH_INVALID_CREDENTIALS"));
        }

        throw new UnauthorizedException("Institute is required", "AUTH_TENANT_REQUIRED");
    }
}
