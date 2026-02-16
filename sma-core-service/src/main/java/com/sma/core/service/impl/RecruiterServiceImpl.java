package com.sma.core.service.impl;

import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.dto.request.user.CreateRecruiterMemberRequest;
import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.CompanyStatus;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.recruiter.RecruiterMapper;
import com.sma.core.repository.CompanyLocationRepository;
import com.sma.core.repository.CompanyRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.UserRepository;
import com.sma.core.service.RecruiterService;
import com.sma.core.service.SubscriptionService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class RecruiterServiceImpl implements RecruiterService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RecruiterRepository recruiterRepository;
    private final CompanyLocationRepository companyLocationRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecruiterMapper recruiterMapper;
    
    // FIXME: Temporarily disabled - causing 500 error
    // private final SubscriptionService subscriptionService;

    @Override
    public void registerRecruiter(RecruiterRegisterRequest request) {

        if (userRepository.findByEmail(request.getRecruiterEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }

        if (companyRepository.existsByTaxIdentificationNumber(
                request.getTaxIdentificationNumber())) {
            throw new AppException(ErrorCode.COMPANY_ALREADY_REGISTERED);
        }

        User user = User.builder()
                .email(request.getRecruiterEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.PENDING_VERIFICATION)
                .role(Role.RECRUITER)
                .build();
        userRepository.save(user);

        Company company = Company.builder()
                .name(request.getCompanyName())
                .description(request.getDescription())
                .companyIndustry(request.getCompanyIndustry())
                .minSize(request.getMinSize())
                .maxSize(request.getMaxSize())
                .email(request.getCompanyEmail())
                .phone(request.getPhone())
                .country(request.getCountry())
                .taxIdentificationNumber(request.getTaxIdentificationNumber())
                .erc(request.getErc())
                .link(request.getCompanyLink())
                .status(CompanyStatus.PENDING_VERIFICATION)
                .build();
        companyRepository.save(company);

        CompanyLocation location = CompanyLocation.builder()
                .company(company)
                .address(request.getAddress())
                .country(request.getCountry())
                .build();
        companyLocationRepository.save(location);

        Recruiter recruiter = Recruiter.builder()
                .user(user)
                .company(company)
                .isVerified(false)
                .isRootRecruiter(true)
                .build();
        recruiterRepository.save(recruiter);
        
        // FIXME: Temporarily disabled - causing 500 error
        // Assign default package for recruiter - commented out due to API error
        // subscriptionService.assignDefaultPlanForCompany(company.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public RecruiterMyInfoResponse getMyInfo() {
        Integer recruiterId = JwtTokenProvider.getCurrentRecruiterId();
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));

        return recruiterMapper.toRecruiterMyInfoResponse(recruiter);
    }

    @Override
    public RecruiterMyInfoResponse createMember(CreateRecruiterMemberRequest request) {
        Recruiter rootRecruiter = recruiterRepository
                .findById(JwtTokenProvider.getCurrentRecruiterId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        if (!Boolean.TRUE.equals(rootRecruiter.getIsRootRecruiter())) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .gender(request.getGender())
                .email(request.getEmail())
                .role(Role.RECRUITER)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        Recruiter recruiterMember = Recruiter.builder()
                .company(rootRecruiter.getCompany())
                .isRootRecruiter(false)
                .isVerified(true)
                .verifiedAt(LocalDateTime.now())
                .user(user)
                .build();
        recruiterRepository.save(recruiterMember);
        return recruiterMapper.toRecruiterMyInfoResponse(recruiterMember);
    }

}
