package com.platform.user.infrastructure.persistence.adapter;

import com.platform.user.domain.model.User;
import com.platform.user.domain.repository.UserRepository;
import com.platform.user.infrastructure.persistence.mapper.UserEntityMapper;
import com.platform.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
