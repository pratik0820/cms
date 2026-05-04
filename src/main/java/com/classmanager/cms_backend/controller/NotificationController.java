package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.entity.Notification;
import com.classmanager.cms_backend.repository.NotificationRepository;
import com.classmanager.cms_backend.security.CmsUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notification management")
public class NotificationController extends BaseController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    @Operation(summary = "Get paginated notification list for current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<Notification>>> getNotifications(
            @AuthenticationPrincipal CmsUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifPage = notificationRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        userDetails.getUserId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.from(notifPage, notifPage.getContent())));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count (for badge)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal CmsUserDetails userDetails) {

        long count = notificationRepository
                .countByUserIdAndIsReadFalseAndIsDeletedFalse(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markOneRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal CmsUserDetails userDetails) {

        notificationRepository.markOneRead(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PatchMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read for current user")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal CmsUserDetails userDetails) {

        notificationRepository.markAllReadForUser(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }
}
