package com.platform.notification.infrastructure.persistence.repository;

import com.platform.notification.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByUserId(UUID userId, Pageable pageable);

    Page<NotificationEntity> findByUserIdAndReadAtIsNull(UUID userId, Pageable pageable);

    Optional<NotificationEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndReadAtIsNull(UUID userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.readAt = :now WHERE n.userId = :userId AND n.readAt IS NULL")
    void markAllReadByUserId(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);
}
