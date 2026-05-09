package com.classmanager.cms_backend.security.jwt;

import com.classmanager.cms_backend.repository.UserRepository;
import com.classmanager.cms_backend.security.CmsUserDetails;
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
        String token = extractTokenFromRequest(request);

        if (token != null && jwtService.isTokenValid(token)) {
            authenticateRequest(request, token);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(HttpServletRequest request, String token) {
        try {
            String userIdStr = jwtService.extractUserId(token);

            if (userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UUID userId = UUID.fromString(userIdStr);

                userRepository.findByIdAndIsDeletedFalse(userId)
                        .ifPresent(user -> {
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
                                log.debug("Authenticated user: {} roles: {}",
                                        user.getEmail(), user.getRoles());
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
                path.startsWith("/api/auth/bootstrap/super-admin") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs");
    }
}
