package com.sma.core.controller;

import com.sma.core.dto.request.company.CompanyFilterRequest;
import com.sma.core.dto.request.company.UpdateCompanyRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.service.CompanyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    final CompanyService companyService;

    @GetMapping
    public ApiResponse<Page<BaseCompanyResponse>> getAllCompanies(@ParameterObject CompanyFilterRequest request) {
        return ApiResponse.<Page<BaseCompanyResponse>>builder()
                .message("Get all companies successfully")
                .data(companyService.getAllCompany(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CompanyDetailResponse> getCompanyById(@PathVariable Integer id) {
        return ApiResponse.<CompanyDetailResponse>builder()
                .message("Get company by id successfully")
                .data(companyService.getCompanyById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BaseCompanyResponse> updateCompanyProfile(@PathVariable Integer id, @RequestBody UpdateCompanyRequest request){
        return ApiResponse.<BaseCompanyResponse>builder()
                .message("Update company profile successfully")
                .data(companyService.updateCompany(id, request))
                .build();
    }

}
