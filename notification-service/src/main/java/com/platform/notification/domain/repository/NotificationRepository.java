package com.platform.notification.domain.repository;

import com.platform.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    Page<Notification> findUnreadByUserId(UUID userId, Pageable pageable);

    long countUnreadByUserId(UUID userId);

    void markAllReadByUserId(UUID userId);
}
