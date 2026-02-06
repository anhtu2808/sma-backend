package com.sma.core.messaging.resume;

import com.sma.core.dto.message.resume.ResumeParsingResultMessage;
import com.sma.core.service.ResumeParsingResultService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResumeParsingResultListener {
    ResumeParsingResultService resumeParsingResultService;

    @RabbitListener(queues = "${app.rabbitmq.resume-parsing.result-queue}")
    public void handleResumeParsingResult(ResumeParsingResultMessage message) {
        try {
            resumeParsingResultService.processParsingResult(message);
        } catch (Exception e) {
            // Keep consumer alive and avoid endless message redelivery loops.
            log.error("Failed to process resume parsing result message: {}", message, e);
        }
    }
}
