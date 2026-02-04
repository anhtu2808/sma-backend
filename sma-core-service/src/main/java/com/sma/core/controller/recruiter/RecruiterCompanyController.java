package com.sma.core.controller.recruiter;

import com.sma.core.service.CompanyService;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

}
