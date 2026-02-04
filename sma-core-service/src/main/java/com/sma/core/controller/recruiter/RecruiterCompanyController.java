package com.sma.core.controller.recruiter;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.job.JobResponse;
import com.sma.core.service.CompanyService;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/recruiter/companies")
@RequiredArgsConstructor
public class RecruiterCompanyController {

    final CompanyService companyService;
    final JobService jobService;

    @GetMapping("/jobs")
    ApiResponse<Page<JobResponse>> getMyCompanyJobs(@ParameterObject JobSearchRequest request) {
        return ApiResponse.<Page<JobResponse>>builder()
                .message("Get my company jobs successfully")
                .data(jobService.getAllJobAsRecruiter(request))
                .build();
    }
}
