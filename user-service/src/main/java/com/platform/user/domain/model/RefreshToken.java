package com.platform.user.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String token;
    private OffsetDateTime expiresAt;
    private boolean revoked;
    private OffsetDateTime createdAt;
}
