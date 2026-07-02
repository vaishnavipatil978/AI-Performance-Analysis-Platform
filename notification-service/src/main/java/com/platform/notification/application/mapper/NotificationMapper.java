package com.platform.notification.application.mapper;

import com.platform.notification.application.dto.response.NotificationResponse;
import com.platform.notification.domain.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "type", expression = "java(notification.getType().name())")
    @Mapping(target = "read", expression = "java(notification.isRead())")
    NotificationResponse toResponse(Notification notification);
}
