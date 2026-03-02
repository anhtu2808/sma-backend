package com.sma.core.service.impl;

import com.sma.core.dto.request.notification.NotificationFilterRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.notification.NotificationListResponse;
import com.sma.core.dto.response.notification.NotificationResponse;
import com.sma.core.entity.Notification;
import com.sma.core.entity.User;
import com.sma.core.enums.NotificationType;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.notification.NotificationMapper;
import com.sma.core.repository.NotificationRepository;
import com.sma.core.repository.UserRepository;
import com.sma.core.service.NotificationService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    NotificationRepository notificationRepository;
    SimpMessagingTemplate messagingTemplate;
    UserRepository userRepository;
    NotificationMapper notificationMapper;
    String USER_QUEUE = "/queue/notifications";

    public void sendAdminNotification(Notification noti) {
        List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        if (admins.isEmpty()) {
            return;
        }

        List<Notification> adminNotifications = admins.stream()
                .map(admin -> Notification.builder()
                        .user(admin)
                        .notificationType(noti.getNotificationType())
                        .title(noti.getTitle())
                        .message(noti.getMessage())
                        .relatedEntityType(noti.getRelatedEntityType())
                        .relatedEntityId(noti.getRelatedEntityId())
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        List<Notification> savedNotifications = notificationRepository.saveAll(adminNotifications);

        for (Notification saved : savedNotifications) {
            NotificationResponse response = notificationMapper.toResponse(saved);

            messagingTemplate.convertAndSendToUser(
                    saved.getUser().getId().toString(),
                    USER_QUEUE,
                    response
            );
        }
    }


    @Transactional
    public void markAsProcessed(Integer relatedEntityId, NotificationType type) {

        notificationRepository.markAllAsReadByEntity(relatedEntityId, type);

        Map<String, Object> signal = new HashMap<>();
        signal.put("action", "MARK_READ_BY_ENTITY");
        signal.put("entityId", relatedEntityId);
        signal.put("type", type);

        messagingTemplate.convertAndSend("/topic/admin-updates", signal);
    }

    @Override
    public NotificationListResponse getMyNotifications(
            NotificationFilterRequest filter,
            Pageable pageable) {

        Integer userId = JwtTokenProvider.getCurrentUserId();
        Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by("createdAt").descending()
        );

        Specification<Notification> spec =
                (root, query, cb) ->
                        cb.equal(root.get("user").get("id"), userId);

        if (filter.getIsRead() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isRead"), filter.getIsRead()));
        }

        if (filter.getType() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("notificationType"), filter.getType()));
        }

        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            String keyword = "%" + filter.getKeyword().toLowerCase() + "%";

            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), keyword),
                            cb.like(cb.lower(root.get("message")), keyword)
                    )
            );
        }

        Page<Notification> page =
                notificationRepository.findAll(spec, sortedPageable);

        List<NotificationResponse> dtoList =
                page.getContent()
                        .stream()
                        .map(notificationMapper::toResponse)
                        .toList();

        PagingResponse<NotificationResponse> pagingData =
                PagingResponse.fromPage(page, dtoList);

        long unreadCount =
                notificationRepository.countByUserIdAndIsReadFalse(userId);

        return NotificationListResponse.builder()
                .notifications(pagingData)
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId) {

        Integer userId = JwtTokenProvider.getCurrentUserId();

        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);

            Map<String, Object> signal = new HashMap<>();
            signal.put("action", "MARK_READ");
            signal.put("notificationId", notificationId);

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    USER_QUEUE,
                    signal
            );
        }
    }

    @Override
    @Transactional
    public void markAllMyNotificationsAsRead() {

        Integer userId = JwtTokenProvider.getCurrentUserId();

        notificationRepository.markAllAsRead(userId);

        Map<String, Object> signal = new HashMap<>();
        signal.put("action", "MARK_ALL_READ");

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                USER_QUEUE,
                signal
        );
    }
}
