package com.platform.gateway.filter;

import com.platform.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/",
            "/actuator/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            sendUnauthorized(response, "Authorization header required");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid or expired JWT token for path: {}", path);
            sendUnauthorized(response, "Invalid or expired token");
            return;
        }

        String userId = jwtUtil.extractSubject(token);
        Claims claims = jwtUtil.getClaims(token);
        String role = claims.get("role", String.class);
        String effectiveRole = (role != null && !role.isBlank()) ? role : "USER";

        log.debug("Authenticated: userId={}, role={}, path={}", userId, effectiveRole, path);

        // Wrap request to inject downstream headers that services expect
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        mutableRequest.putHeader("X-User-ID", userId);
        mutableRequest.putHeader("X-User-Role", effectiveRole);

        // Set Spring Security context so @PreAuthorize on gateway routes works
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + effectiveRole)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            chain.doFilter(mutableRequest, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format(
                "{\"success\":false,\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message, LocalDateTime.now()));
    }
}
