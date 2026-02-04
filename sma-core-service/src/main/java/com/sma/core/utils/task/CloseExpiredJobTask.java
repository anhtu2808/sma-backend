package com.sma.core.utils.task;

import com.sma.core.repository.JobRepository;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CloseExpiredJobTask {

    final JobService jobService;

    @Scheduled(cron = "0 59 23 * * *")
    public void closeExpiredJobs() {
        jobService.closeExpiredJob();
    }

}
