package com.platform.user.domain.repository;

import com.platform.user.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(UUID userId);
}
