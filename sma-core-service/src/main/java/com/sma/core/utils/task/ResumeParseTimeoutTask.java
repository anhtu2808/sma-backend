package com.sma.core.utils.task;

import com.sma.core.entity.Resume;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.repository.UsageEventRepository;
import com.sma.core.service.ResumeParsingResultService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeParseTimeoutTask {

    final ResumeRepository resumeRepository;
    final UsageEventRepository usageEventRepository;
    final ResumeParsingResultService resumeParsingResultService;

    @Value("${app.resume-parsing.partial-timeout-minutes:1.5}")
    double partialTimeoutMinutes;

    @Scheduled(fixedDelayString = "${app.resume-parsing.watchdog-interval-ms:30000}")
    public void markTimedOutParsesAsFailed() {
        double timeoutMinutes = Math.max(0.25d, partialTimeoutMinutes);
        long timeoutMillis = Math.round(timeoutMinutes * 60_000d);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.minus(Duration.ofMillis(timeoutMillis));
        String processedAt = OffsetDateTime.now().toString();

        List<Resume> timedOutResumes = resumeRepository.findTimedOutParses(
                ResumeParseStatus.PARTIAL,
                deadline
        );

        int affectedRows = 0;
        for (Resume resume : timedOutResumes) {
            try {
                Integer usageEventId = usageEventRepository
                        .findLatestResumeParsingUsageEventId(resume.getId(), resume.getParseRequestedAt())
                        .orElse(null);

                resumeParsingResultService.markParseFailed(
                        resume.getId(),
                        usageEventId,
                        resume.getParseAttemptId(),
                        "TIMEOUT",
                        processedAt
                );
                affectedRows++;
            } catch (Exception exception) {
                log.error("Failed to mark timed out resume parse as FAIL for resumeId={}", resume.getId(), exception);
            }
        }

        if (affectedRows > 0) {
            log.warn(
                    "Marked {} resume parse tasks as FAIL due to timeout ({} minutes)",
                    affectedRows,
                    timeoutMinutes
            );
        }
    }
}
