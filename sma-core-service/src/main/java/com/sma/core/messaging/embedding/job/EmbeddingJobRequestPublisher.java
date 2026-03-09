package com.sma.core.messaging.embedding.job;

import com.sma.core.config.RabbitMQProperties.EmbeddingJob;
import com.sma.core.config.RabbitMQProperties.EmbeddingResume;
import com.sma.core.dto.message.embedding.job.EmbeddingJobRequestMessage;
import com.sma.core.dto.message.embedding.resume.EmbeddingResumeRequestMessage;
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
public class EmbeddingJobRequestPublisher {

    RabbitTemplate rabbitTemplate;
    EmbeddingJob embeddingJob;

    public void publish(EmbeddingJobRequestMessage message) {
        log.info("Publishing embedding job request for jobId={}",
                message.getId());
        rabbitTemplate.convertAndSend(embeddingJob.getRequestQueue(), message);
        log.info("Embedding job request published successfully for jobId={}", message.getId());
    }

}
