package com.classmanager.cms_backend.notification;

import com.classmanager.cms_backend.entity.Notification;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.repository.NotificationRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import com.classmanager.cms_backend.service.BaseService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationAsyncService extends BaseService {

    private static final Logger log = LogManager.getLogger(NotificationAsyncService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    private static final String NOTIFICATION_TOPIC = "/topic/user/{userId}/notifications";

    @Async
    @Transactional
    public void sendToUser(UUID userId, String title, String message,
                           String type, UUID referenceId, String referenceType) {
        try {
            User user = userRepository.findByIdAndIsDeletedFalse(userId).orElse(null);
            if (user == null) {
                log.warn("Cannot send notification — user not found: {}", userId);
                return;
            }

            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type(type)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .isRead(false)
                    .build();

            notification.setTenantId(user.getTenantId());
            notificationRepository.save(notification);

            NotificationPayload payload = NotificationPayload.builder()
                    .id(notification.getId())
                    .title(title)
                    .message(message)
                    .type(type)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .createdAt(notification.getCreatedAt())
                    .build();

            String destination = NOTIFICATION_TOPIC.replace("{userId}", userId.toString());
            messagingTemplate.convertAndSend(destination, payload);

            log.debug("Notification sent to user {}: {}", userId, title);

        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage(), e);
        }
    }
}
