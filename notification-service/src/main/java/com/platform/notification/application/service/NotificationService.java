package com.platform.notification.application.service;

import com.platform.common.dto.PageResponse;
import com.platform.notification.application.dto.response.NotificationResponse;
import com.platform.notification.application.dto.response.UnreadCountResponse;
import com.platform.notification.domain.event.AnalysisCompletedEvent;

import java.util.UUID;

public interface NotificationService {

    void handleAnalysisCompleted(AnalysisCompletedEvent event);

    PageResponse<NotificationResponse> getUserNotifications(UUID userId, int page, int size);

    PageResponse<NotificationResponse> getUnreadNotifications(UUID userId, int page, int size);

    UnreadCountResponse getUnreadCount(UUID userId);

    NotificationResponse markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);
}
