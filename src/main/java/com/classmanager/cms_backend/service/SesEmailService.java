package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.properties.SesProperties;
import com.classmanager.cms_backend.service.jpa.EmailService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aws.ses", name = "enabled", havingValue = "true")
public class SesEmailService implements EmailService {

    private static final Logger log = LogManager.getLogger(SesEmailService.class);

    private final SesClient sesClient;
    private final SesProperties sesProperties;

    @Override
    public void send(String to, String subject, String bodyText) {
        if (!sesProperties.isEnabled()) {
            log.info("SES disabled. Skipping email to {}", to);
            return;
        }

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(sesProperties.getFromEmail())
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .text(Content.builder().data(bodyText).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(request);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send SES email to {}", to, e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody, String fallbackText) {
        if (!sesProperties.isEnabled()) {
            log.info("SES disabled. Skipping email to {}", to);
            return;
        }

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(sesProperties.getFromEmail())
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                    .text(Content.builder().data(fallbackText).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(request);
            log.info("HTML email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send SES HTML email to {}", to, e);
        }
    }
}
