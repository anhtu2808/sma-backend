package com.sma.core.service.impl;

import com.sma.core.dto.request.company.CompanyFilterRequest;
import com.sma.core.dto.request.company.UpdateCompanyRequest;
import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.dto.request.company.CompanyVerificationRequest;
import com.sma.core.entity.Company;
import com.sma.core.entity.CompanyImage;
import com.sma.core.entity.Recruiter;
import com.sma.core.enums.CompanyStatus;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.CompanyRepository;
import com.sma.core.repository.JobRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.service.CompanyService;
import com.sma.core.dto.request.company.CompanyFilterRequest;
import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.enums.Role;
import com.sma.core.mapper.company.CompanyMapper;
import com.sma.core.specification.CompanySpecification;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    CompanyRepository companyRepository;
    RecruiterRepository recruiterRepository;
    CompanyMapper companyMapper;
    JobRepository jobRepository;

    @Override
    @Transactional
    public void updateRegistrationStatus(Integer companyId, CompanyVerificationRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        CompanyStatus currentStatus = company.getStatus();
        CompanyStatus nextStatus = request.getStatus();

        if (currentStatus == CompanyStatus.APPROVED || currentStatus == CompanyStatus.REJECTED) {
            throw new AppException(ErrorCode.STATUS_ALREADY_FINALIZED);
        }

        if (currentStatus == CompanyStatus.PENDING_VERIFICATION && nextStatus != CompanyStatus.UNDER_REVIEW) {
            throw new AppException(ErrorCode.MUST_BE_UNDER_REVIEW_FIRST);
        }

        if (currentStatus == CompanyStatus.UNDER_REVIEW) {
            if (nextStatus != CompanyStatus.APPROVED && nextStatus != CompanyStatus.REJECTED) {
                throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
            }
        }

        company.setStatus(nextStatus);

        if (nextStatus == CompanyStatus.REJECTED) {
            company.setRejectReason(request.getReason());
        } else {
            company.setRejectReason(null);
        }

        Recruiter recruiter = recruiterRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (nextStatus == CompanyStatus.APPROVED) {
            recruiter.setIsVerified(true);
            recruiter.setVerifiedAt(LocalDateTime.now());
            recruiter.getUser().setStatus(UserStatus.ACTIVE);

        } else if (nextStatus == CompanyStatus.REJECTED) {
            recruiter.setIsVerified(false);
            recruiter.getUser().setStatus(UserStatus.INACTIVE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDetailResponse getCompanyById(Integer id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));
        Role role = JwtTokenProvider.getCurrentRole();

        // internal users (Admin, Recruiter) or Owner can see more details or restricted
        // companies
        boolean isInternal = role != null && (role.equals(Role.ADMIN) || role.equals(Role.RECRUITER));

        if (!isInternal) {
            EnumSet<CompanyStatus> allowedStatus = EnumSet.of(CompanyStatus.APPROVED);
            if (!allowedStatus.contains(company.getStatus()))
                throw new AppException(ErrorCode.COMPANY_NOT_AVAILABLE);
        }

        CompanyDetailResponse response = isInternal
                ? companyMapper.toInternalCompanyResponse(company)
                : companyMapper.toCompanyDetailResponse(company);

        response.setTotalJobs(jobRepository.countByCompanyId(id));

        return response;
    }

    @Override
    public BaseCompanyResponse updateCompany(Integer id, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));
        companyMapper.updateToCompany(request, company);
        companyRepository.save(company);
        return companyMapper.toBaseCompanyResponse(company);
    }

    @Override
    public Page<BaseCompanyResponse> getAllCompany(CompanyFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<CompanyStatus> allowedStatus = EnumSet.of(CompanyStatus.APPROVED);
        Role role = JwtTokenProvider.getCurrentRole();
        if (role != null && (role.equals(Role.RECRUITER) || role.equals(Role.ADMIN))) {
            if (!request.getStatus().isEmpty()) {
                allowedStatus = request.getStatus();
            } else {
                allowedStatus = EnumSet.allOf(CompanyStatus.class);
            }
        }

        return companyRepository.findAll(CompanySpecification.withFilter(request, allowedStatus), pageable)
                .map(companyMapper::toBaseCompanyResponse);
    }

}
