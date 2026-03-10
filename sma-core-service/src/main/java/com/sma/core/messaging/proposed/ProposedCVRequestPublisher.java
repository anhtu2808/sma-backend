package com.sma.core.messaging.proposed;

import com.sma.core.config.RabbitMQProperties.MatchingRabbitMQProperties;
import com.sma.core.config.RabbitMQProperties.ProposedCV;
import com.sma.core.dto.message.matching.MatchingRequestMessage;
import com.sma.core.dto.message.proposed.ProposedCVRequestMessage;
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
public class ProposedCVRequestPublisher {

    RabbitTemplate rabbitTemplate;
    ProposedCV proposedCV;

    public void publish(ProposedCVRequestMessage message) {
        log.info("Proposed CV request for jobId={}",
                 message.getId());
        rabbitTemplate.convertAndSend(proposedCV.getRequestQueue(), message);
        log.info("Proposed CV request published successfully for jobId={}", message.getId());
    }

}
