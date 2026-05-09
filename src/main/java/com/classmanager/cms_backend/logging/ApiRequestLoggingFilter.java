package com.classmanager.cms_backend.logging;

import com.classmanager.cms_backend.security.CmsUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(ApiRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        ThreadContext.put("requestId", requestId);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = extractClientIp(request);

        log.info("API request started: method={} path={} query={} clientIp={} user={}",
                method,
                path,
                queryString != null ? queryString : "-",
                clientIp,
                resolveCurrentUser());

        try {
            filterChain.doFilter(request, response);
        } finally {
            stopWatch.stop();
            log.info("API request completed: method={} path={} status={} durationMs={} user={}",
                    method,
                    path,
                    response.getStatus(),
                    stopWatch.getTotalTimeMillis(),
                    resolveCurrentUser());
            ThreadContext.remove("requestId");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/actuator");
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CmsUserDetails userDetails)) {
            return "anonymous";
        }
        return userDetails.getEmail() + "(" + userDetails.getUserId() + ")";
    }
}
