package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.BootstrapSuperAdminRequest;
import com.classmanager.cms_backend.dto.request.ChangePasswordRequest;
import com.classmanager.cms_backend.dto.request.LoginRequest;
import com.classmanager.cms_backend.dto.request.RefreshTokenRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.AuthResponse;
import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final AuthService authService;

    @PostMapping("/bootstrap/super-admin")
    @Operation(summary = "Create the initial super admin account when the system is fresh")
    public ResponseEntity<ApiResponse<AuthResponse>> bootstrapSuperAdmin(
            @Valid @RequestBody BootstrapSuperAdminRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.bootstrapSuperAdmin(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Super admin created successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
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

        AuthResponse.UserInfo info = authService.buildUserInfo(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
