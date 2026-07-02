package com.platform.notification.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private UUID referenceId;
    private String referenceType;
    private OffsetDateTime readAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;

    public boolean isRead() {
        return readAt != null;
    }
}
