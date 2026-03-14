package com.sma.core.messaging.embedding;

import com.sma.core.dto.message.embedding.EmbeddingResultMessage;
import com.sma.core.dto.message.suggest.SuggestResultMessage;
import com.sma.core.service.JobService;
import com.sma.core.service.ResumeService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmbeddingResultListener {

    JobService jobService;
    ResumeService resumeService;

    @RabbitListener(queues = "${app.rabbitmq.embedding.resume.result-queue}")
    public void handleEmbeddingResumeResult(EmbeddingResultMessage message) {
        try {
            resumeService.updateEmbeddingResume(message);
        } catch (Exception e) {
            log.error("Failed to embedding resume result message: {}", message, e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.embedding.job.result-queue}")
    public void handleEmbeddingJobResult(EmbeddingResultMessage message) {
        try {
            jobService.updateEmbeddingJob(message);
        } catch (Exception e) {
            log.error("Failed to embedding job result message: {}", message, e);
        }
    }

}
