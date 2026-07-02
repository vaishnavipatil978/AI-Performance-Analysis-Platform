package com.platform.user.infrastructure.persistence.adapter;

import com.platform.user.domain.model.RefreshToken;
import com.platform.user.domain.repository.RefreshTokenRepository;
import com.platform.user.infrastructure.persistence.mapper.RefreshTokenEntityMapper;
import com.platform.user.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenEntityMapper mapper;

    @Override
    public RefreshToken save(RefreshToken token) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(token)));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
