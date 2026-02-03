package com.sma.core.service.impl;

import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.entity.*;
import com.sma.core.enums.CompanyStatus;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.*;
import com.sma.core.service.RecruiterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RoleRepository roleRepository;
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

        Role recruiterRole = roleRepository.findByNameIgnoreCase("RECRUITER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        User user = User.builder()
                .email(request.getRecruiterEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.INACTIVE)
                .roles(Set.of(recruiterRole))
                .build();
        userRepository.save(user);

        Company company = Company.builder()
                .name(request.getCompanyName())
                .description(request.getDescription())
                .companyIndustry(request.getCompanyIndustry())
                .size(request.getSize())
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
                .isRootCandidate(true)
                .build();
        recruiterRepository.save(recruiter);
    }
}
