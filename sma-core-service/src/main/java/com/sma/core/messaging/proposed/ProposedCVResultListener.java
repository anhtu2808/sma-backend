package com.sma.core.messaging.proposed;

import com.sma.core.dto.message.suggest.SuggestResultMessage;
import com.sma.core.service.JobService;
import com.sma.core.service.ResumeEvaluationService;
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
public class ProposedCVResultListener {

    JobService jobService;

    @RabbitListener(queues = "${app.rabbitmq.proposed.cv.result-queue}")
    public void handleMatchingSuggestionResult(SuggestResultMessage message) {
        try {
           // jobService.saveSuggestion(message);
        } catch (Exception e) {
            log.error("Failed to process matching result message: {}", message, e);
        }
    }

}
