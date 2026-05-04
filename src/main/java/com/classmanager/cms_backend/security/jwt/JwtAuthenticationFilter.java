package com.classmanager.cms_backend.security.jwt;

import com.classmanager.cms_backend.repository.UserRepository;
import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);

            if (token != null && jwtService.isTokenValid(token)) {
                authenticateRequest(request, token);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void authenticateRequest(HttpServletRequest request, String token) {
        try {
            String userIdStr = jwtService.extractUserId(token);
            UUID tokenTenantId = jwtService.extractTenantId(token);

            if (userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UUID userId = UUID.fromString(userIdStr);

                userRepository.findByIdAndIsDeletedFalse(userId)
                        .ifPresent(user -> {

                            if (!user.getTenantId().equals(tokenTenantId)) {
                                log.error("Tenant mismatch! tokenTenantId={} actualTenantId={}",
                                        tokenTenantId, user.getTenantId());
                                throw new SecurityException("Invalid tenant access");
                            }

                            TenantContext.setCurrentTenant(user.getTenantId());

                            CmsUserDetails userDetails = new CmsUserDetails(user);

                            if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {

                                UsernamePasswordAuthenticationToken authToken =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );
                                authToken.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request)
                                );
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                                log.debug("Authenticated user: {} tenant: {} role: {}",
                                        user.getEmail(), user.getTenantId(), user.getRoles());
                            }
                        });
            }
        } catch (Exception e) {
            log.error("Error setting user authentication: {}", e.getMessage());
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/tenants/register") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs");
    }
}
