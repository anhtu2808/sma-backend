package com.sma.core.controller;

import com.sma.core.dto.request.notification.NotificationFilterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.notification.NotificationListResponse;
import com.sma.core.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;

    @GetMapping
    public ApiResponse<NotificationListResponse> getAllNotifications(
            NotificationFilterRequest filter,
            @ParameterObject Pageable pageable) {
        return ApiResponse.<NotificationListResponse>builder()
                .message("Get notifications successfully")
                .data(notificationService.getMyNotifications(filter, pageable))
                .build();
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ApiResponse.<Void>builder()
                .message("Marked notification as read")
                .build();
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllMyNotificationsAsRead();
        return ApiResponse.<Void>builder()
                .message("Marked all notifications as read")
                .build();
    }
}
