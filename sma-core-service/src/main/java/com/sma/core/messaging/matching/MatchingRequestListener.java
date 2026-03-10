package com.sma.core.messaging.matching;

import com.sma.core.dto.message.matching.MatchingResultMessage;
import com.sma.core.enums.EvaluationStatus;
import com.sma.core.repository.ResumeEvaluationRepository;
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
    ResumeEvaluationRepository resumeEvaluationRepository;

    @RabbitListener(queues = "${app.rabbitmq.matching.result-queue}")
    public void handleMatchingResult(MatchingResultMessage message) {
        try {
            evaluationService.processMatchingResult(message);
        } catch (Exception e) {
            log.error("Failed to process matching result for evaluationId={}: {}",
                    message.getEvaluationId(), e.getMessage(), e);
            // Save FAIL status outside the rolled-back transaction
            markEvaluationFailed(message.getEvaluationId());
        }
    }

    private void markEvaluationFailed(Integer evaluationId) {
        if (evaluationId == null) return;
        try {
            resumeEvaluationRepository.findById(evaluationId).ifPresent(evaluation -> {
                evaluation.setEvaluationStatus(EvaluationStatus.FAIL);
                resumeEvaluationRepository.save(evaluation);
                log.info("Marked evaluationId={} as FAIL after processing error", evaluationId);
            });
        } catch (Exception ex) {
            log.error("Failed to mark evaluationId={} as FAIL", evaluationId, ex);
        }
    }

}
