package com.sma.core.dto.response.notification;

import com.sma.core.enums.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Integer id;
    String title;
    String message;
    NotificationType notificationType;
    String relatedEntityType;
    Integer relatedEntityId;
    Boolean isRead;
    LocalDateTime createdAt;
}
