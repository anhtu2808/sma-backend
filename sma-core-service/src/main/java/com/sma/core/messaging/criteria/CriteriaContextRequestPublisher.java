package com.sma.core.messaging.criteria;

import com.sma.core.config.RabbitMQProperties.CriteriaContextRabbitMQProperties;
import com.sma.core.dto.message.criteria.CriteriaContextRequestMessage;
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
public class CriteriaContextRequestPublisher {

    RabbitTemplate rabbitTemplate;
    CriteriaContextRabbitMQProperties criteriaContextRabbitMQProperties;

    public void publish(CriteriaContextRequestMessage message) {
        log.info("Publishing criteria context request for jobId={}, types={}",
                message.getJobId(), message.getCriteriaTypes());
        rabbitTemplate.convertAndSend(criteriaContextRabbitMQProperties.getRequestQueue(), message);
        log.info("Criteria context request published successfully for jobId={}", message.getJobId());
    }
}
