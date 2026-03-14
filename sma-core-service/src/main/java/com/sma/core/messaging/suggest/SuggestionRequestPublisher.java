package com.sma.core.messaging.suggest;

import com.sma.core.config.RabbitMQProperties.MatchingRabbitMQProperties;
import com.sma.core.config.RabbitMQProperties.ReSuggestionRabbitMQProperties;
import com.sma.core.config.RabbitMQProperties.SuggestionRabbitMQProperties;
import com.sma.core.dto.message.matching.MatchingRequestMessage;
import com.sma.core.dto.message.suggest.ReSuggestRequestMessage;
import com.sma.core.dto.message.suggest.SuggestionRequestMessage;
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
public class SuggestionRequestPublisher {

    RabbitTemplate rabbitTemplate;
    SuggestionRabbitMQProperties suggestionRabbitMQProperties;
    ReSuggestionRabbitMQProperties reSuggestionRabbitMQProperties;

    public void publish(SuggestionRequestMessage message) {
        log.info("Publishing suggestion request for evaluationId={}, jobId={}, resumeId={}",
                message.getEvaluationId(), message.getJobId(), message.getResumeId());
        rabbitTemplate.convertAndSend(suggestionRabbitMQProperties.getRequestQueue(), message);
        log.info("Suggestion request published successfully for evaluationId={}", message.getEvaluationId());
    }

    public void publish(ReSuggestRequestMessage message) {
        rabbitTemplate.convertAndSend(reSuggestionRabbitMQProperties.getRequestQueue(), message);
    }


}
