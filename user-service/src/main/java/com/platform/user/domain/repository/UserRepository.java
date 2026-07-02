package com.platform.user.domain.repository;

import com.platform.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteById(UUID id);

    Page<User> findAll(Pageable pageable);

    long count();
}
