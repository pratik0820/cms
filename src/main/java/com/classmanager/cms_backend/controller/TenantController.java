package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.RegisterTenantRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.entity.Tenant;
import com.classmanager.cms_backend.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "SaaS tenant (institute) registration and management")
public class TenantController extends BaseController {

    private static final Logger log = LogManager.getLogger(TenantController.class);
    private final TenantService tenantService;

    @PostMapping("/register")
    @Operation(summary = "Register a new institute (public endpoint)")
    public ResponseEntity<ApiResponse<Tenant>> register(
            @Valid @RequestBody RegisterTenantRequest request) {

        log.info("Received tenant registration request for subdomain={}", request.getSubdomain());

        Tenant tenant = tenantService.registerTenant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(tenant, "Institute registered successfully. Check your email for login details."));
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get tenant by ID (Super Admin only)")
    public ResponseEntity<ApiResponse<Tenant>> getTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.getTenantById(tenantId)));
    }

    @PostMapping("/{tenantId}/activate")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Activate a tenant (Super Admin only)")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID tenantId) {
        tenantService.activateTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(null, "Tenant activated"));
    }

    @PostMapping("/{tenantId}/deactivate")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Deactivate a tenant (Super Admin only)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID tenantId) {
        tenantService.deactivateTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(null, "Tenant deactivated"));
    }
}
