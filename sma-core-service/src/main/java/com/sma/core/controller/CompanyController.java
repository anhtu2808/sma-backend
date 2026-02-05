package com.sma.core.controller;

import com.sma.core.dto.request.company.CompanyFilterRequest;
import com.sma.core.dto.request.company.CompanyVerificationRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.company.AdminCompanyResponse;
import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.dto.response.company.CompanyResponse;
import com.sma.core.enums.CompanyStatus;
import com.sma.core.service.CompanyService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @PatchMapping("/{companyId}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Integer companyId,
            @RequestBody @Valid CompanyVerificationRequest request) {

        companyService.updateRegistrationStatus(companyId, request);
        return ApiResponse.<Void>builder()
                .message("Company status update successfully: " + request.getStatus())
                .build();
    }

    @GetMapping("/admin-view")
    public ApiResponse<Page<AdminCompanyResponse>> getAllCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CompanyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminCompanyResponse> companies = companyService.getAllCompaniesForAdmin(name, status, pageable);

        return ApiResponse.<Page<AdminCompanyResponse>>builder()
                .data(companies)
                .build();
    }

    @GetMapping("/admin-view/{companyId}")
    public ApiResponse<CompanyResponse> getCompanyDetail(@PathVariable Integer companyId) {
        CompanyResponse companyDetail = companyService.getCompanyDetailForAdmin(companyId);

        return ApiResponse.<CompanyResponse>builder()
                .data(companyDetail)
                .build();
    }

}
