package com.sma.core.service;

import com.sma.core.dto.request.company.CompanySearchRequest;
import com.sma.core.dto.response.company.CompanyResponse;
import org.springframework.data.domain.Page;

public interface CompanyService {

    Page<CompanyResponse> getAllCompany(CompanySearchRequest request);
    CompanyResponse getCompanyById(Integer id);

}
