package com.classmanager.cms_backend.schedular;

import com.classmanager.cms_backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private static final Logger log = LogManager.getLogger(CleanupScheduler.class);

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        try {
            refreshTokenRepository.deleteExpiredBefore(cutoff);
            log.info("Cleanup: expired refresh tokens deleted (older than {})", cutoff);
        } catch (Exception e) {
            log.error("Error cleaning up refresh tokens: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 2 1 * *")
    public void generateMonthlyPayouts() {
        log.info("Scheduled: Monthly payout calculation triggered for {}", LocalDateTime.now().getMonth());
        // TODO: Inject TeacherPayoutService and call generateMonthlyPayouts()
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendFeeOverdueNotifications() {
        log.info("Scheduled: Fee overdue notification check triggered");
        // TODO: Inject FeeService and call sendOverdueNotifications()
    }
}
