package com.sma.core.service;

import com.sma.core.dto.request.company.CompanyFilterRequest;
import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import org.springframework.data.domain.Page;

public interface CompanyService {

    Page<BaseCompanyResponse> getAllCompany(CompanyFilterRequest request);
    CompanyDetailResponse getCompanyById(Integer id);

}
