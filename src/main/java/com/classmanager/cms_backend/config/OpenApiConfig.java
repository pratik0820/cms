package com.classmanager.cms_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 / Swagger UI configuration.
 * <p>
 * Access at:
 *   - Swagger UI: <a href="http://localhost:8080/swagger-ui.html">...</a>
 *   - API JSON:   <a href="http://localhost:8080/api-docs">...</a>
 * <p>
 * JWT auth is pre-configured — click "Authorize" in Swagger UI,
 * paste your Bearer token once, and it applies to all endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${swagger.local-url}")
    private String localUrl;

    @Value("${swagger.production-url}")
    private String productionUrl;

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Class Management System API")
                        .version("1.0.0")
                        .description("""
                                SaaS Class Management System — REST API
                                
                                **Authentication:** All endpoints except /auth/login, /auth/refresh,
                                and /tenants/register require a Bearer JWT token.
                                
                                **Multi-tenancy:** The tenantId is embedded in the JWT — no need to
                                pass it separately. All data is automatically scoped to your institute.
                                
                                **Roles:** SUPER_ADMIN | ADMIN | TEACHER | STUDENT | PARENT
                                """)
                        .contact(new Contact()
                                .name("CMS Support")
                                .email("support@cms.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://cms.com/terms")))
                .servers(List.of(
                        new Server().url(localUrl).description("Local Development"),
                        new Server().url(productionUrl).description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT access token here (without 'Bearer ' prefix)")));
    }
}
