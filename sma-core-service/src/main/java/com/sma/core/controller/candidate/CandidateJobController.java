package com.sma.core.controller.candidate;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/candidate/jobs")
@RequiredArgsConstructor
public class CandidateJobController {

    final JobService jobService;

    @GetMapping("/saved")
    public ApiResponse<Page<BaseJobResponse>> getMySavedJob() {
        return ApiResponse.<Page<BaseJobResponse>>builder()
                .message("Get my saved job successfully")
                .data(jobService.getAllMySavedJob())
                .build();
    }

}
