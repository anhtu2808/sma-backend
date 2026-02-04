package com.sma.core.service;

import com.sma.core.dto.request.company.CompanyVerificationRequest;
import com.sma.core.dto.response.company.AdminCompanyResponse;
import com.sma.core.dto.response.company.CompanyResponse;
import com.sma.core.enums.CompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService {
    Page<AdminCompanyResponse> getAllCompaniesForAdmin(String name, CompanyStatus status, Pageable pageable);
    void updateRegistrationStatus(Integer companyId, CompanyVerificationRequest request);
    CompanyResponse getCompanyDetailForAdmin(Integer companyId);
}
