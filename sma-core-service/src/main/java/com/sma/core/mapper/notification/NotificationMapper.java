package com.sma.core.mapper.notification;

import com.sma.core.dto.response.notification.NotificationResponse;
import com.sma.core.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
