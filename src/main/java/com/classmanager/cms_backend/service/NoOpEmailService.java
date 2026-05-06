package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.service.jpa.EmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "aws.ses", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEmailService implements EmailService {

    private static final Logger log = LogManager.getLogger(NoOpEmailService.class);

    @Override
    public void send(String to, String subject, String bodyText) {
        log.info("No email provider configured. Skipping text email to {}", to);
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody, String fallbackText) {
        log.info("No email provider configured. Skipping HTML email to {}", to);
    }
}
