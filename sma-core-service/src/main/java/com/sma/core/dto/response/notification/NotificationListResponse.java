package com.sma.core.dto.response.notification;

import com.sma.core.dto.response.PagingResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationListResponse {
    PagingResponse<NotificationResponse> notifications;
    long unreadCount;
}
