package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID userId, Pageable pageable);

    long countByUserIdAndIsReadFalseAndIsDeletedFalse(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.user.id = :userId AND n.isRead = false")
    void markAllReadForUser(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.id = :id AND n.user.id = :userId")
    void markOneRead(@Param("id") UUID id, @Param("userId") UUID userId);
}
