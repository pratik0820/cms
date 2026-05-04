package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.PlanType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterTenantRequest {

    @NotBlank(message = "Institute name is required")
    @Size(min = 2, max = 100, message = "Institute name must be 2-100 characters")
    private String instituteName;

    /**
     * Subdomain for the institute: institutename.yourapp.com
     * Only lowercase alphanumeric and hyphens allowed.
     */
    @NotBlank(message = "Subdomain is required")
    @Pattern(regexp = "^[a-z0-9-]{3,50}$",
            message = "Subdomain must be 3-50 chars, lowercase letters, numbers, hyphens only")
    private String subdomain;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Please provide a valid email address")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminPassword;

    @NotBlank(message = "Admin name is required")
    private String adminName;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    private PlanType planType = PlanType.STARTER;
}
