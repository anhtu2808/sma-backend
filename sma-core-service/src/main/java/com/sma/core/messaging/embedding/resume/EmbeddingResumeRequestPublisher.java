package com.sma.core.messaging.embedding.resume;

import com.sma.core.config.RabbitMQProperties.EmbeddingResume;
import com.sma.core.config.RabbitMQProperties.MatchingRabbitMQProperties;
import com.sma.core.dto.message.embedding.resume.EmbeddingResumeRequestMessage;
import com.sma.core.dto.message.matching.MatchingRequestMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmbeddingResumeRequestPublisher {

    RabbitTemplate rabbitTemplate;
    EmbeddingResume embeddingResume;

    public void publish(EmbeddingResumeRequestMessage message) {
        log.info("Publishing embedding resume request for resumeId={}",
                message.getId());
        rabbitTemplate.convertAndSend(embeddingResume.getRequestQueue(), message);
        log.info("Matching request published successfully for resumeId={}", message.getId());
    }

}
