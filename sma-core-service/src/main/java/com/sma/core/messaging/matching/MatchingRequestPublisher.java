package com.sma.core.messaging.matching;

import com.sma.core.config.RabbitMQProperties.MatchingRabbitMQProperties;
import com.sma.core.dto.message.matching.MatchingRequestMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MatchingRequestPublisher {

    RabbitTemplate rabbitTemplate;
    MatchingRabbitMQProperties matchingRabbitMQProperties;

    public void publish(MatchingRequestMessage message) {
        log.info("Publishing matching request for evaluationId={}, jobId={}, resumeId={}",
                message.getEvaluationId(), message.getJobId(), message.getResumeId());
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("evaluationId", message.getEvaluationId());
        context.put("jobId", message.getJobId());
        context.put("resumeId", message.getResumeId());
        context.put("matchingType", message.getMatchingType());
        rabbitTemplate.convertAndSend(matchingRabbitMQProperties.getRequestQueue(), message);
        log.info("Matching request published successfully for evaluationId={}", message.getEvaluationId());
    }

}
