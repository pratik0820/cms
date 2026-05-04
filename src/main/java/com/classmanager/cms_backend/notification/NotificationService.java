package com.classmanager.cms_backend.notification;

import com.classmanager.cms_backend.entity.Notification;
import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.repository.NotificationRepository;
import com.classmanager.cms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LogManager.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationAsyncService asyncService;

    private static final String NOTIFICATION_TOPIC = "/topic/user/{userId}/notifications";
    private static final String TEST_RESULT = "TEST_RESULT";

    public void sendToUser(UUID userId, String title, String message, String type) {
        asyncService.sendToUser(userId, title, message, type, null, null);
    }

    public void sendToUsers(List<UUID> userIds, String title, String message, String type) {
        userIds.forEach(uid -> asyncService.sendToUser(uid, title, message, type, null, null));
    }

    public void notifyNewAdmission(UUID adminUserId, String studentName) {
        sendToUser(adminUserId,
                "New Admission",
                "Student '" + studentName + "' has been admitted.",
                "ADMISSION");
    }

    public void notifyFeePaymentRecorded(UUID adminUserId, String studentName, double amount) {
        sendToUser(adminUserId,
                "Fee Payment Recorded",
                "₹" + String.format("%.0f", amount) + " fee collected for " + studentName,
                "FEE_PAYMENT");
    }

    public void notifyFeeOverdue(UUID adminUserId, UUID parentUserId,
                                 String studentName, double pendingAmount) {
        String msg = "Fee of ₹" + String.format("%.0f", pendingAmount) + " is overdue for " + studentName;
        sendToUser(adminUserId, "Fee Overdue", msg, "FEE_OVERDUE");
        if (parentUserId != null) {
            sendToUser(parentUserId, "Fee Due Reminder",
                    "Your child " + studentName + " has a pending fee of ₹" +
                            String.format("%.0f", pendingAmount) + ". Please pay at the earliest.",
                    "FEE_OVERDUE");
        }
    }

    public void notifyTeacherClassIn(UUID adminUserId, String teacherName, String batchName) {
        sendToUser(adminUserId,
                "Class Started",
                teacherName + " has started class for " + batchName,
                "CLASS_IN");
    }

    public void notifyTeacherClassOut(UUID adminUserId, String teacherName,
                                      String batchName, int durationMins) {
        sendToUser(adminUserId,
                "Class Ended",
                teacherName + " ended class for " + batchName +
                        " (" + durationMins + " mins)",
                "CLASS_OUT");
    }

    public void notifyHomeworkAssigned(List<UUID> studentUserIds, List<UUID> parentUserIds,
                                       String subject, String batchName) {
        String msg = "New homework assigned for " + subject + " (" + batchName + ")";
        sendToUsers(studentUserIds, "Homework Assigned", msg, "HOMEWORK");
        sendToUsers(parentUserIds, "Homework Assigned", msg, "HOMEWORK");
    }

    public void notifyTestAvailable(List<UUID> studentUserIds, String testTitle, String subject) {
        sendToUsers(studentUserIds,
                "New Test Available",
                "A new test '" + testTitle + "' is available for " + subject,
                "TEST_AVAILABLE");
    }

    public void notifyTestResultUploaded(UUID studentUserId, UUID parentUserId,
                                         UUID adminUserId, String testTitle,
                                         double percentage) {
        String msg = "Result uploaded for '" + testTitle + "': " +
                String.format("%.1f", percentage) + "%";
        sendToUser(studentUserId, "Test Result", msg, TEST_RESULT);
        if (parentUserId != null) sendToUser(parentUserId, "Test Result", msg, TEST_RESULT);
        if (adminUserId != null) sendToUser(adminUserId, "Test Result Uploaded", msg, TEST_RESULT);
    }

    public void notifyStudentAbsent(UUID parentUserId, String studentName, String date) {
        sendToUser(parentUserId,
                "Attendance Alert",
                studentName + " was marked absent on " + date,
                "ATTENDANCE_ABSENT");
    }

    public void notifyNewInquiry(UUID adminUserId, String studentName, String source) {
        sendToUser(adminUserId,
                "New Inquiry",
                "New admission inquiry from " + studentName + " via " + source,
                "NEW_INQUIRY");
    }

    public void notifyNewTimetable(UUID teacherUserId, String message) {
        sendToUser(teacherUserId, "Timetable Updated", message, "TIMETABLE");
    }
}
