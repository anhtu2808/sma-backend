package com.sma.core.messaging.criteria;

import com.sma.core.dto.message.criteria.CriteriaContextResultMessage;
import com.sma.core.entity.ScoringCriteria;
import com.sma.core.repository.ScoringCriteriaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CriteriaContextResultListener {

    ScoringCriteriaRepository scoringCriteriaRepository;

    @RabbitListener(queues = "${app.rabbitmq.criteria-context.result-queue}")
    public void handleCriteriaContextResult(CriteriaContextResultMessage message) {
        try {
            log.info("Received criteria context result for jobId={}, status={}",
                    message.getJobId(), message.getStatus());

            if (!"SUCCESS".equals(message.getStatus())) {
                log.warn("Criteria context generation failed for jobId={}: {}",
                        message.getJobId(), message.getErrorMessage());
                return;
            }

            Map<String, String> contexts = message.getContexts();
            Map<String, Integer> typeToId = message.getCriteriaTypeToScoringCriteriaId();

            if (contexts == null || typeToId == null) {
                log.warn("Empty contexts or mapping in criteria context result for jobId={}", message.getJobId());
                return;
            }

            for (Map.Entry<String, String> entry : contexts.entrySet()) {
                String criteriaType = entry.getKey();
                String context = entry.getValue();
                Integer scoringCriteriaId = typeToId.get(criteriaType);

                if (scoringCriteriaId == null) {
                    log.warn("No scoring criteria ID found for type={}", criteriaType);
                    continue;
                }

                ScoringCriteria sc = scoringCriteriaRepository.findById(scoringCriteriaId).orElse(null);
                if (sc == null) {
                    log.warn("ScoringCriteria not found for id={}", scoringCriteriaId);
                    continue;
                }

                sc.setContext(context);
                scoringCriteriaRepository.save(sc);
                log.info("Updated context for scoringCriteriaId={}, type={}", scoringCriteriaId, criteriaType);
            }

            log.info("Successfully processed criteria context for jobId={}, {} criteria updated",
                    message.getJobId(), contexts.size());

        } catch (Exception e) {
            log.error("Failed to process criteria context result for jobId={}", message.getJobId(), e);
        }
    }
}
