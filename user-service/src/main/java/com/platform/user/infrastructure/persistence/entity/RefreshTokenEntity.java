package com.platform.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
