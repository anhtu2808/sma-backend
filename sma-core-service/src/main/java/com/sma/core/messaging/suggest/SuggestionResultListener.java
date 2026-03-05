package com.sma.core.messaging.suggest;

import com.sma.core.dto.message.suggest.SuggestResultMessage;
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
public class SuggestionResultListener {

    ResumeEvaluationService resumeEvaluationService;

    @RabbitListener(queues = "${app.rabbitmq.suggest.result-queue}")
    public void handleMatchingResult(SuggestResultMessage message) {
        try {
            resumeEvaluationService.saveSuggestion(message);
        } catch (Exception e) {
            log.error("Failed to process matching result message: {}", message, e);
        }
    }

}
