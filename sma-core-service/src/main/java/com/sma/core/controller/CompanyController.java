package com.sma.core.controller;

import com.sma.core.dto.request.company.CompanySearchRequest;
import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.company.CompanyResponse;
import com.sma.core.dto.response.job.JobResponse;
import com.sma.core.service.CompanyService;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    final CompanyService companyService;
    final JobService jobService;

    @GetMapping
    public ApiResponse<Page<CompanyResponse>> getAllCompanies(@ParameterObject CompanySearchRequest request) {
        return ApiResponse.<Page<CompanyResponse>>builder()
                .message("Get all companies successfully")
                .data(companyService.getAllCompany(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CompanyResponse> getCompanyById(@PathVariable Integer id) {
        return ApiResponse.<CompanyResponse>builder()
                .message("Get company by id successfully")
                .data(companyService.getCompanyById(id))
                .build();
    }

}
