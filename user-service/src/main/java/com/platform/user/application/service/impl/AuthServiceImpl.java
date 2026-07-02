package com.platform.user.application.service.impl;

import com.platform.common.exception.UnauthorizedException;
import com.platform.user.application.dto.request.LoginRequest;
import com.platform.user.application.dto.request.RegisterRequest;
import com.platform.user.application.dto.response.AuthResponse;
import com.platform.user.application.mapper.UserMapper;
import com.platform.user.application.service.AuthService;
import com.platform.user.domain.exception.EmailAlreadyExistsException;
import com.platform.user.domain.exception.InvalidCredentialsException;
import com.platform.user.domain.exception.UserNotFoundException;
import com.platform.user.domain.model.RefreshToken;
import com.platform.user.domain.model.Role;
import com.platform.user.domain.model.User;
import com.platform.user.domain.repository.RefreshTokenRepository;
import com.platform.user.domain.repository.UserRepository;
import com.platform.user.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.USER)
                .active(true)
                .emailVerified(false)
                .build();
        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getEmail());
        return issueTokenPair(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        log.info("User logged in: {}", user.getEmail());
        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (token.isRevoked() || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new UnauthorizedException("Refresh token has expired or been revoked");
        }
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UserNotFoundException(token.getUserId()));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Token refreshed for user: {}", user.getEmail());
        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public void logout(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("User logged out, token revoked");
        });
    }

    private AuthResponse issueTokenPair(User user) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "email", user.getEmail()
        );
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().toString(), claims);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId().toString());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenValue)
                .expiresAt(OffsetDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshTokenExpiryMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenValue,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiryMs() / 1000,
                userMapper.toResponse(user)
        );
    }
}
