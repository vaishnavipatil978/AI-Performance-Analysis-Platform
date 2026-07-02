package com.platform.notification.infrastructure.persistence.adapter;

import com.platform.notification.domain.model.Notification;
import com.platform.notification.domain.repository.NotificationRepository;
import com.platform.notification.infrastructure.persistence.mapper.NotificationEntityMapper;
import com.platform.notification.infrastructure.persistence.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationEntityMapper mapper;

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(notification)));
    }

    @Override
    public Optional<Notification> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public Page<Notification> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Notification> findUnreadByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndReadAtIsNull(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    public void markAllReadByUserId(UUID userId) {
        jpaRepository.markAllReadByUserId(userId, OffsetDateTime.now());
    }
}
