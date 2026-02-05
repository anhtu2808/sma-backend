package com.sma.core.service.impl;
import com.sma.core.dto.request.company.CompanyVerificationRequest;
import com.sma.core.dto.response.company.AdminCompanyResponse;
import com.sma.core.dto.response.company.CompanyResponse;
import com.sma.core.dto.response.company.LocationShortResponse;
import com.sma.core.dto.response.recruiter.RecruiterShortResponse;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    CompanyRepository companyRepository;
    RecruiterRepository recruiterRepository;
    CompanyMapper companyMapper;
    JobRepository jobRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCompanyResponse> getAllCompaniesForAdmin(String name, CompanyStatus status, Pageable pageable) {
        Page<Company> companies;
        if (name != null && status != null) {
            companies = companyRepository.findByNameContainingIgnoreCaseAndStatus(name, status, pageable);
        } else if (name != null) {
            companies = companyRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (status != null) {
            companies = companyRepository.findByStatus(status, pageable);
        } else {
            companies = companyRepository.findAll(pageable);
        }

        return companies.map(company -> {
            AdminCompanyResponse response = companyMapper.toAdminResponse(company);
            response.setRecruiterCount(recruiterRepository.countByCompanyId(company.getId()));
            return response;
        });
    }

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
    public CompanyResponse getCompanyDetailForAdmin(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        CompanyResponse response = companyMapper.toDetailResponse(company);

        response.setRecruiters(company.getRecruiters().stream()
                .map(r -> RecruiterShortResponse.builder()
                        .id(r.getId())
                        .avatar(r.getUser().getAvatar())
                        .fullName(r.getUser().getFullName())
                        .email(r.getUser().getEmail())
                        .isRootCandidate(r.getIsRootCandidate())
                        .isVerified(r.getIsVerified())
                        .build())
                .toList());

        response.setLocations(company.getLocations().stream()
                .map(l -> LocationShortResponse.builder()
                        .name(l.getName())
                        .address(l.getAddress())
                        .district(l.getDistrict())
                        .city(l.getCity())
                        .country(l.getCountry())
                        .googleMapLink(l.getGoogleMapLink())
                        .build())
                .toList());

        List<String> imageUrls = company.getImages().stream()
                .map((CompanyImage img) -> img.getUrl())
                .toList();
        response.setImages(imageUrls);

        response.setTotalJobs(jobRepository.countByCompanyId(companyId));

        return response;
    }

    @Override
    public CompanyDetailResponse getCompanyById(Integer id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));
        Role role = JwtTokenProvider.getCurrentRole();
        // handle restrict candidate access to INACTIVE, SUSPENDED, PENDING_VERIFICATION company
        if (role == null || role.equals(Role.CANDIDATE)) {
            EnumSet<CompanyStatus> allowedStatus = EnumSet.of(CompanyStatus.APPROVED);
            if(!allowedStatus.contains(company.getStatus()))
                throw new AppException(ErrorCode.COMPANY_NOT_AVAILABLE);
            return companyMapper.toCompanyDetailResponse(company);
        }
        return companyMapper.toInternalCompanyResponse(company);
    }

    @Override
    public Page<BaseCompanyResponse> getAllCompany(CompanyFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<CompanyStatus> allowedStatus = EnumSet.of(CompanyStatus.APPROVED);
        return companyRepository.findAll(CompanySpecification.withFilter(request, allowedStatus), pageable)
                .map(companyMapper::toBaseCompanyResponse);
    }

}
