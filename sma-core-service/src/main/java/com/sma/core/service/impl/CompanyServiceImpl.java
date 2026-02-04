package com.sma.core.service.impl;

import com.sma.core.dto.request.company.CompanySearchRequest;
import com.sma.core.dto.response.company.CompanyResponse;
import com.sma.core.repository.CompanyRepository;
import com.sma.core.service.CompanyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    final CompanyRepository companyRepository;

    @Override
    public Page<CompanyResponse> getAllCompany(CompanySearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return null;
    }

    @Override
    public CompanyResponse getCompanyById(Integer id) {
        return null;
    }
}
