package com.sma.core.dto.request.notification;

import com.sma.core.enums.NotificationType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationFilterRequest {
    Boolean isRead;
    List<NotificationType> types;
    String keyword;
}