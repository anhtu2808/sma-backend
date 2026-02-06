package com.sma.core.service;

import com.sma.core.dto.request.company.CompanyVerificationRequest;
import com.sma.core.dto.request.company.CompanyFilterRequest;
import com.sma.core.dto.request.company.UpdateCompanyRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;

public interface CompanyService {

    void updateRegistrationStatus(Integer companyId, CompanyVerificationRequest request);

    PagingResponse<BaseCompanyResponse> getAllCompany(CompanyFilterRequest request);

    CompanyDetailResponse getCompanyById(Integer id);

    CompanyDetailResponse updateCompany(Integer id, UpdateCompanyRequest request);
}
