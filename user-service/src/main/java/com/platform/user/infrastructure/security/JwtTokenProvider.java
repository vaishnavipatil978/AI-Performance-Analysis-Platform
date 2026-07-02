package com.platform.user.infrastructure.security;

import com.platform.common.security.JwtProperties;
import com.platform.common.security.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private JwtUtil jwtUtil;

    @PostConstruct
    public void init() {
        this.jwtUtil = new JwtUtil(
                jwtProperties.getSecret(),
                jwtProperties.getAccessTokenExpiryMs(),
                jwtProperties.getRefreshTokenExpiryMs()
        );
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return jwtUtil.generateAccessToken(subject, claims);
    }

    public String generateRefreshToken(String subject) {
        return jwtUtil.generateRefreshToken(subject);
    }

    public boolean isTokenValid(String token) {
        return jwtUtil.isTokenValid(token);
    }

    public String extractSubject(String token) {
        return jwtUtil.extractSubject(token);
    }

    public long getAccessTokenExpiryMs() {
        return jwtProperties.getAccessTokenExpiryMs();
    }

    public long getRefreshTokenExpiryMs() {
        return jwtProperties.getRefreshTokenExpiryMs();
    }
}
