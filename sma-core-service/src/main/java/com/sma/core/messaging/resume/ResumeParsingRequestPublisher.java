package com.sma.core.messaging.resume;

import com.sma.core.config.ResumeParsingRabbitMQProperties;
import com.sma.core.dto.message.resume.ResumeParsingRequestMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResumeParsingRequestPublisher {
    RabbitTemplate rabbitTemplate;
    ResumeParsingRabbitMQProperties resumeParsingRabbitMQProperties;

    public void publish(Integer resumeId, String resumeUrl, String fileName, String resumeName) {
        ResumeParsingRequestMessage message = ResumeParsingRequestMessage.builder()
                .resumeId(resumeId)
                .resumeUrl(resumeUrl)
                .fileName(fileName)
                .resumeName(resumeName)
                .requestedAt(Instant.now().toString())
                .build();

        rabbitTemplate.convertAndSend(resumeParsingRabbitMQProperties.getRequestQueue(), message);
        log.info("Published resume parsing request to queue for resumeId={}", resumeId);
    }
}
