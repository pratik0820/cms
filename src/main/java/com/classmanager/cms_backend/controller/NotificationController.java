package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.NotificationResponse;
import com.classmanager.cms_backend.entity.Notification;
import com.classmanager.cms_backend.repository.NotificationRepository;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for managing user notifications")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'TEACHER', 'STUDENT')")
public class NotificationController extends BaseController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    @Operation(summary = "Get all notifications for the current authenticated user")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        UUID userId = currentUserId();
        List<NotificationResponse> responses = notificationRepository
                .findByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a specific notification as read")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        
        // Ensure user owns the notification
        if (!notification.getUser().getId().equals(currentUserId())) {
            throw new com.classmanager.cms_backend.exception.BadRequestException("You can only read your own notifications", "UNAUTHORIZED_ACTION");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications for the current user as read")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        UUID userId = currentUserId();
        List<Notification> notifications = notificationRepository.findByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }
}
