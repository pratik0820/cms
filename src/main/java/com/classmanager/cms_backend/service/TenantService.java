package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.PlanConfig;
import com.classmanager.cms_backend.dto.request.RegisterTenantRequest;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.Role;
import com.classmanager.cms_backend.entity.Tenant;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.enums.PlanType;
import com.classmanager.cms_backend.enums.UserRole;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BranchRepository;
import com.classmanager.cms_backend.repository.RoleRepository;
import com.classmanager.cms_backend.repository.TenantRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import com.classmanager.cms_backend.service.jpa.EmailService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService extends BaseService {

    private static final Logger log = LogManager.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public Tenant registerTenant(RegisterTenantRequest request) {

        String normalizedSubdomain = normalizeSubdomain(request.getSubdomain());

        log.info("Starting tenant registration for subdomain={}", normalizedSubdomain);

        try {

            validateRequest(request, normalizedSubdomain);

            PlanConfig planConfig = getPlanConfig(request.getPlanType());

            Tenant tenant = Tenant.builder()
                    .name(request.getInstituteName().trim())
                    .subdomain(normalizedSubdomain)
                    .contactEmail(request.getAdminEmail().toLowerCase().trim())
                    .contactPhone(request.getContactPhone())
                    .planType(request.getPlanType())
                    .isActive(true)
                    .maxStudents(planConfig.getMaxStudents())
                    .maxBranches(planConfig.getMaxBranches())
                    .trialEndsAt(LocalDateTime.now().plusDays(14))
                    .build();

            tenant = tenantRepository.save(tenant);
            UUID tenantId = tenant.getId();

            log.info("Tenant created successfully. tenantId={}", tenantId);

            // 2. Create default branch "Main"
            Branch defaultBranch = Branch.builder()
                    .name("Main")
                    .city(null)
                    .isActive(true)
                    .build();
            defaultBranch.setTenantId(tenantId);

            defaultBranch = branchRepository.save(defaultBranch);

            log.info("Default branch created. branchId={}, tenantId={}",
                    defaultBranch.getId(), tenantId);

            Role tenantOwnerRole = roleRepository.findByName(UserRole.TENANT_OWNER.name())
                    .or(() -> roleRepository.findByName(UserRole.SUPER_ADMIN.name()))
                    .orElseThrow(() -> new RuntimeException("Role TENANT_OWNER not found"));

            // 3. Create Super Admin user for this tenant
            User adminUser = User.builder()
                    .email(request.getAdminEmail())
                    .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                    .roles(Set.of(tenantOwnerRole))
                    .fullName(request.getAdminName())
                    .phone(request.getContactPhone())
                    .branch(defaultBranch)
                    .isActive(true)
                    .build();
            adminUser.setTenantId(tenantId);
            userRepository.saveAndFlush(adminUser);

            log.info("New tenant registered: {} ({}), subdomain: {}",
                    tenant.getName(), tenantId, tenant.getSubdomain());

            sendSuperAdminWelcomeEmail(request, tenant.getName());

            return tenant;
        } catch (DataIntegrityViolationException ex) {
            log.error("Duplicate entry error during tenant registration. subdomain={}", normalizedSubdomain, ex);
            throw new ResourceAlreadyExistsException("Subdomain already exists.");
        } catch (Exception ex) {
            log.error("Unexpected error during tenant registration. subdomain={}", normalizedSubdomain, ex);
            throw ex;
        }
    }

    public Tenant getTenantById(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
    }

    @Transactional
    public void deactivateTenant(UUID tenantId) {
        Tenant tenant = getTenantById(tenantId);
        tenant.setIsActive(false);
        tenantRepository.save(tenant);
        log.info("Tenant deactivated: {} ({})", tenant.getName(), tenantId);
    }

    @Transactional
    public void activateTenant(UUID tenantId) {
        Tenant tenant = getTenantById(tenantId);
        tenant.setIsActive(true);
        tenantRepository.save(tenant);
        log.info("Tenant activated: {} ({})", tenant.getName(), tenantId);
    }

    private String normalizeSubdomain(String subdomain) {
        if (subdomain == null) {
            throw new BadRequestException("Subdomain is required");
        }

        return subdomain.trim().toLowerCase();
    }

    private void validateRequest(RegisterTenantRequest request, String subdomain) {

        if (tenantRepository.existsBySubdomain(subdomain)) {
            throw new ResourceAlreadyExistsException(
                    "Subdomain '" + subdomain + "' is already taken.");
        }

        if (request.getPlanType() == null) {
            throw new BadRequestException("Plan type is required");
        }

        if (request.getAdminPassword() == null || request.getAdminPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
    }

    private PlanConfig getPlanConfig(PlanType planType) {

        return switch (planType) {
            case STARTER -> new PlanConfig(100, 1);
            case GROWTH -> new PlanConfig(500, 3);
            case ENTERPRISE -> new PlanConfig(Integer.MAX_VALUE, Integer.MAX_VALUE);
        };
    }

    private void sendSuperAdminWelcomeEmail(RegisterTenantRequest request, String instituteName) {
        try {
            Map<String, String> vars = new HashMap<>();
            vars.put("fullName", request.getAdminName());
            vars.put("instituteName", instituteName);
            vars.put("loginUrl", frontendUrl);
            vars.put("loginEmail", request.getAdminEmail());
            vars.put("temporaryPassword", request.getAdminPassword());

            String subject = emailTemplateService.render("super-admin/super-admin-welcome.subject.txt", vars).trim();
            String html = emailTemplateService.render("super-admin/super-admin-welcome.html", vars);
            String text = emailTemplateService.render("super-admin/super-admin-welcome.txt", vars);

            emailService.sendHtml(request.getAdminEmail(), subject, html, text);
        } catch (Exception ex) {
            log.error("Super admin created but welcome email failed for email={}", request.getAdminEmail(), ex);
        }
    }

}
