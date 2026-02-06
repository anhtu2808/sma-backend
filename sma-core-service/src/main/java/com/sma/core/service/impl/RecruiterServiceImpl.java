package com.sma.core.service.impl;

import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.dto.request.company.CompanyVerificationRequest;
import com.sma.core.entity.Company;
import com.sma.core.entity.CompanyLocation;
import com.sma.core.entity.Recruiter;
import com.sma.core.entity.User;
import com.sma.core.enums.CompanyStatus;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.CompanyLocationRepository;
import com.sma.core.repository.CompanyRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.UserRepository;
import com.sma.core.service.RecruiterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Set;
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
                .status(UserStatus.INACTIVE)
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
    }

}
