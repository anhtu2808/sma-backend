package com.sma.core.service;

import org.thymeleaf.context.Context;

public interface EmailService {
    void sendEmail(String to, String subject, String htmlBody);
    void sendEmailWithTemplate(String to, String subject, String templateName, Context context);
}
