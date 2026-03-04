package com.sma.core.messaging.matching;

import com.sma.core.config.RabbitMQProperties.MatchingRabbitMQProperties;
import com.sma.core.dto.message.matching.MatchingResultMessage;
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
public class MatchingRequestListener {

    ResumeEvaluationService evaluationService;

    @RabbitListener(queues = "${app.rabbitmq.matching.result-queue}")
    public void handleMatchingResult(MatchingResultMessage message) {
        try {
            evaluationService.processMatchingResult(message);
        } catch (Exception e) {
            log.error("Failed to process matching result message: {}", message, e);
        }
    }

}
