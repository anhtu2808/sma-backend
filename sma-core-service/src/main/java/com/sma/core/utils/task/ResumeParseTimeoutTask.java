package com.sma.core.utils.task;

import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.repository.ResumeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeParseTimeoutTask {

    final ResumeRepository resumeRepository;

    @Value("${app.resume-parsing.partial-timeout-minutes:1.5}")
    double partialTimeoutMinutes;

    @Scheduled(fixedDelayString = "${app.resume-parsing.watchdog-interval-ms:30000}")
    @Transactional
    public void markTimedOutParsesAsFailed() {
        double timeoutMinutes = Math.max(0.25d, partialTimeoutMinutes);
        long timeoutMillis = Math.round(timeoutMinutes * 60_000d);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.minus(Duration.ofMillis(timeoutMillis));

        int affectedRows = resumeRepository.markTimedOutParses(
                ResumeParseStatus.PARTIAL,
                ResumeParseStatus.FAIL,
                ResumeStatus.DRAFT,
                "TIMEOUT",
                now,
                deadline
        );

        if (affectedRows > 0) {
            log.warn(
                    "Marked {} resume parse tasks as FAIL due to timeout ({} minutes)",
                    affectedRows,
                    timeoutMinutes
            );
        }
    }
}
