package com.platform.notification.application.service.impl;

import com.platform.common.dto.PageResponse;
import com.platform.common.exception.ResourceNotFoundException;
import com.platform.notification.application.dto.response.NotificationResponse;
import com.platform.notification.application.dto.response.UnreadCountResponse;
import com.platform.notification.application.mapper.NotificationMapper;
import com.platform.notification.application.service.NotificationService;
import com.platform.notification.domain.event.AnalysisCompletedEvent;
import com.platform.notification.domain.model.Notification;
import com.platform.notification.domain.model.NotificationType;
import com.platform.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void handleAnalysisCompleted(AnalysisCompletedEvent event) {
        boolean succeeded = "COMPLETED".equals(event.status());
        Notification notification = Notification.builder()
                .userId(event.userId())
                .type(succeeded ? NotificationType.ANALYSIS_COMPLETED : NotificationType.ANALYSIS_FAILED)
                .title(succeeded
                        ? "Analysis Complete — " + event.findingsCount() + " finding(s)"
                        : "Analysis Failed")
                .message(succeeded
                        ? "Your performance report has been analysed. " + event.findingsCount()
                                + " issue(s) were identified. View your results to see recommendations."
                        : "The AI analysis of your report encountered an error. Please try again or contact support.")
                .referenceId(event.analysisId())
                .referenceType("ANALYSIS")
                .build();
        notificationRepository.save(notification);
        log.info("Notification created for user={}, type={}", event.userId(), notification.getType());
    }

    @Override
    public PageResponse<NotificationResponse> getUserNotifications(UUID userId, int page, int size) {
        return PageResponse.from(notificationRepository.findByUserId(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(notificationMapper::toResponse));
    }

    @Override
    public PageResponse<NotificationResponse> getUnreadNotifications(UUID userId, int page, int size) {
        return PageResponse.from(notificationRepository.findUnreadByUserId(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(notificationMapper::toResponse));
    }

    @Override
    public UnreadCountResponse getUnreadCount(UUID userId) {
        return new UnreadCountResponse(notificationRepository.countUnreadByUserId(userId));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId.toString()));
        if (!notification.isRead()) {
            notification.setReadAt(OffsetDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
        log.info("Marked all notifications as read for user={}", userId);
    }
}
