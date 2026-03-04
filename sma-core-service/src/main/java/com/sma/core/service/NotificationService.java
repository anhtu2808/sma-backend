package com.sma.core.service;

import com.sma.core.dto.request.notification.NotificationFilterRequest;
import com.sma.core.dto.response.notification.NotificationListResponse;
import com.sma.core.entity.Notification;
import com.sma.core.entity.User;
import com.sma.core.enums.NotificationType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void sendAdminNotification(Notification noti);
    void markAsProcessed(Integer relatedEntityId, NotificationType type);
    NotificationListResponse getMyNotifications(NotificationFilterRequest filter, Pageable pageable);
    void markAsRead(Integer notificationId);
    void markAllMyNotificationsAsRead();
    void sendCandidateNotification(User user, NotificationType type, String title, String message, String entityType, Integer entityId);
    void sendRecruiterNotification(Integer companyId, NotificationType type, String title, String message, String entityType, Integer entityId);
}
