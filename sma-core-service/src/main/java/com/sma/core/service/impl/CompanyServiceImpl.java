package com.sma.core.service.impl;

import com.sma.core.dto.request.company.CompanyFilterRequest;
import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.entity.Company;
import com.sma.core.enums.CompanyStatus;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.company.CompanyMapper;
import com.sma.core.repository.CompanyRepository;
import com.sma.core.specification.CompanySpecification;
import com.sma.core.service.CompanyService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    final CompanyRepository companyRepository;
    final CompanyMapper companyMapper;

    @Override
    public CompanyDetailResponse getCompanyById(Integer id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));
        Role role = JwtTokenProvider.getCurrentRole();
        // handle restrict candidate access to INACTIVE, SUSPENDED, PENDING_VERIFICATION company
        if (role == null || role.equals(Role.CANDIDATE)) {
            EnumSet<CompanyStatus> allowedStatus = EnumSet.of(CompanyStatus.ACTIVE);
            if(!allowedStatus.contains(company.getStatus()))
                throw new AppException(ErrorCode.COMPANY_NOT_AVAILABLE);
            return companyMapper.toCompanyDetailResponse(company);
        }
        return companyMapper.toInternalCompanyResponse(company);
    }

    @Override
    public Page<BaseCompanyResponse> getAllCompany(CompanyFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<CompanyStatus> allowedStatus = EnumSet.of(CompanyStatus.ACTIVE);
        return companyRepository.findAll(CompanySpecification.withFilter(request, allowedStatus), pageable)
                .map(companyMapper::toBaseCompanyResponse);
    }

}
