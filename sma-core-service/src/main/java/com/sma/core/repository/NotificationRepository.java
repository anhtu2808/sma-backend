package com.sma.core.repository;

import com.sma.core.entity.Notification;
import com.sma.core.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer>, JpaSpecificationExecutor<Notification> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    long countByUserIdAndIsReadFalse(Integer userId);
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true " +
            "WHERE n.relatedEntityId = :entityId " +
            "AND n.notificationType = :type " +
            "AND n.isRead = false")
    void markAllAsReadByEntity(@Param("entityId") Integer entityId,
                               @Param("type") NotificationType type);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Integer userId);
    Optional<Notification> findByIdAndUserId(Integer id, Integer userId);
}
