package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.ChangePasswordRequest;
import com.classmanager.cms_backend.dto.request.LoginRequest;
import com.classmanager.cms_backend.dto.request.RefreshTokenRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.AuthResponse;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.exception.UnauthorizedException;
import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, token refresh, and password management")
public class AuthController extends BaseController {

    private static final Logger log = LogManager.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        try {
            AuthResponse response = authService.login(request, httpRequest);

            log.info("Login successful for email: {}", request.getEmail());

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Login successful")
            );

        } catch (UnauthorizedException ex) {

            log.warn("Invalid login attempt for email: {}", request.getEmail());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(
                    ApiResponse.error("Invalid email or password")
            );

        } catch (Exception ex) {

            log.error("Unexpected error during login for email: {}",
                    request.getEmail(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(
                    ApiResponse.error("Something went wrong. Please try again later.")
            );
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }

    // ─── AUTHENTICATED ENDPOINTS ─────────────────────────────────────────────

    @PostMapping("/logout")
    @Operation(summary = "Logout from current device (revoke refresh token)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices (revoke all refresh tokens)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            @AuthenticationPrincipal CmsUserDetails userDetails) {

        authService.logoutAllDevices(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out from all devices"));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change current user's password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CmsUserDetails userDetails) {

        authService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(
            @AuthenticationPrincipal CmsUserDetails userDetails) {

        List<String> roles = userDetails.getRoles().stream()
                .map(Role::getName)
                .toList();

        AuthResponse.UserInfo info = AuthResponse.UserInfo.builder()
                .id(userDetails.getUserId())
                .email(userDetails.getEmail())
                .roles(roles)
                .tenantId(userDetails.getTenantId())
                .branchId(userDetails.getBranchId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
