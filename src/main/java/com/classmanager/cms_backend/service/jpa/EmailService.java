package com.classmanager.cms_backend.service.jpa;

public interface EmailService {

    void send(String to, String subject, String bodyText);

    void sendHtml(String to, String subject, String htmlBody, String fallbackText);
}
