package com.sma.core.service.impl;

import com.sma.core.dto.request.subscription.CreateSubscriptionRequest;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.Company;
import com.sma.core.entity.Package;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.PaymentMethod;
import com.sma.core.enums.Role;
import com.sma.core.enums.SubscriptionStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.CandidateRepository;
import com.sma.core.repository.PackageRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.SubscriptionRepository;
import com.sma.core.service.PaymentService;
import com.sma.core.service.SubscriptionService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    final SubscriptionRepository subscriptionRepository;
    final PackageRepository packageRepository;
    final CandidateRepository candidateRepository;
    final RecruiterRepository recruiterRepository;
    final PaymentService paymentService;

    @Override
    public String createSubscription(CreateSubscriptionRequest request) {
        Package existedPackage = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_EXIST));
        Subscription subscription = Subscription.builder()
                .startDate(LocalDateTime.now())
                .price(existedPackage.getSalePrice())
                .packageEntity(existedPackage)
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .build();
        if (Objects.equals(JwtTokenProvider.getCurrentRole(), Role.CANDIDATE)){
            Candidate candidate = candidateRepository
                    .findById(JwtTokenProvider.getCurrentCandidateId())
                    .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));
            subscription.setCandidate(candidate);
        } else {
            Company company = recruiterRepository
                    .findById(JwtTokenProvider.getCurrentRecruiterId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED))
                    .getCompany();
            subscription.setCompany(company);
        }
        String qr = paymentService.createQR(subscription, PaymentMethod.SEPAY);
        subscriptionRepository.save(subscription);
        return qr;
    }
}
