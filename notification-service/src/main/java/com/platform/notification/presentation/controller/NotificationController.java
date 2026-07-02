package com.platform.notification.presentation.controller;

import com.platform.common.dto.ApiResponse;
import com.platform.common.dto.PageResponse;
import com.platform.notification.application.dto.response.NotificationResponse;
import com.platform.notification.application.dto.response.UnreadCountResponse;
import com.platform.notification.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification feed")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications for current user")
    public ApiResponse<PageResponse<NotificationResponse>> getNotifications(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Notifications retrieved",
                notificationService.getUserNotifications(userId, page, size));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ApiResponse<PageResponse<NotificationResponse>> getUnreadNotifications(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Unread notifications retrieved",
                notificationService.getUnreadNotifications(userId, page, size));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(@RequestHeader("X-User-ID") UUID userId) {
        return ApiResponse.success("Unread count retrieved", notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID userId) {
        return ApiResponse.success("Notification marked as read", notificationService.markAsRead(id, userId));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ApiResponse<Void> markAllAsRead(@RequestHeader("X-User-ID") UUID userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("All notifications marked as read", null);
    }
}
