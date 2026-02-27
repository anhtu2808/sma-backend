package com.sma.core.service.impl;

import com.sma.core.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SesClient sesClient;
    private final TemplateEngine templateEngine;

    @Override
    public void sendEmail(String to, String subject, String htmlBody) {

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder()
                            .toAddresses(to)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).build())
                                    .build())
                            .build())
                    .source("SmartRecruit <no-reply@smartrecruit.tech>")
                    .build();

            sesClient.sendEmail(request);

            System.out.println("Email sent successfully to " + to);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }


    @Async
    @Override
    public void sendEmailWithTemplate(String to, String subject, String templateName, Context context) {
        String htmlBody = templateEngine.process(templateName, context);
        sendEmail(to, subject, htmlBody);
    }
}
