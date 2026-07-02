package com.platform.notification.infrastructure.persistence.mapper;

import com.platform.notification.domain.model.Notification;
import com.platform.notification.infrastructure.persistence.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationEntityMapper {

    NotificationEntity toEntity(Notification domain);

    Notification toDomain(NotificationEntity entity);
}
