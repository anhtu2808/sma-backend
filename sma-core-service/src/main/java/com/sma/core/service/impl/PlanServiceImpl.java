package com.sma.core.service.impl;

import com.sma.core.dto.request.plan.PlanCreateRequest;
import com.sma.core.dto.request.plan.PlanUpdateRequest;
import com.sma.core.dto.request.plan.PlanFilterRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.plan.PlanResponse;
import com.sma.core.dto.response.planprice.PlanPriceResponse;
import com.sma.core.entity.Plan;
import com.sma.core.entity.PlanPrice;
import com.sma.core.entity.Recruiter;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
import com.sma.core.enums.Role;
import com.sma.core.enums.SubscriptionStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.plan.PlanMapper;
import com.sma.core.mapper.plan.PlanPriceMapper;
import com.sma.core.repository.PlanPriceRepository;
import com.sma.core.repository.PlanRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.SubscriptionRepository;
import com.sma.core.service.PlanService;
import com.sma.core.specification.PlanSpecification;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PlanServiceImpl implements PlanService {

    PlanRepository planRepository;
    PlanPriceRepository planPriceRepository;
    SubscriptionRepository subscriptionRepository;
    RecruiterRepository recruiterRepository;
    PlanMapper planMapper;
    PlanPriceMapper planPriceMapper;

    @Override
    public PlanResponse createPlan(PlanCreateRequest request) {
        if (planRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.PLAN_ALREADY_EXISTS);
        }

        Plan plan = planMapper.toEntity(request);
        plan.setCurrency(normalizeCurrency(request.getCurrency()));
        plan.setIsActive(false);
        plan.setIsPopular(Boolean.TRUE.equals(request.getIsPopular()));
        plan.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        plan = planRepository.save(plan);
        if (Boolean.TRUE.equals(plan.getIsDefault())) {
            planRepository.clearDefaultByTarget(plan.getPlanTarget(), plan.getId());
        }

        return buildResponse(plan, null, null);
    }

    @Override
    public PlanResponse updatePlan(Integer id, PlanUpdateRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        boolean hasSubscription = subscriptionRepository.existsByPlanId(id);

        if (hasSubscription) {
            if (!isUpdateOnlyDefault(plan, request)) {
                throw new AppException(ErrorCode.PLAN_UPDATE_ONLY_PRICE_ALLOWED);
            }
            if (request.getIsDefault() != null) {
                plan.setIsDefault(request.getIsDefault());
                planRepository.save(plan);
                if (Boolean.TRUE.equals(request.getIsDefault())) {
                    planRepository.clearDefaultByTarget(plan.getPlanTarget(), plan.getId());
                }
            }
            return buildResponse(plan, null, null);
        }

        planMapper.updateFromRequest(request, plan);
        plan.setCurrency(normalizeCurrency(request.getCurrency()));
        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }
        if (request.getIsPopular() != null) {
            plan.setIsPopular(request.getIsPopular());
        }

        planRepository.save(plan);
        if (Boolean.TRUE.equals(plan.getIsDefault())) {
            planRepository.clearDefaultByTarget(plan.getPlanTarget(), plan.getId());
        }
        return buildResponse(plan, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse getPlanById(Integer id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        Role role = JwtTokenProvider.getCurrentRole();
        Integer currentPlanId = resolveCurrentPlanId(role);
        return buildResponse(plan, role, currentPlanId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<PlanResponse> getPlans(PlanFilterRequest request) {
        Role role = JwtTokenProvider.getCurrentRole();
        PlanTarget resolvedTarget = request.getPlanTarget();
        PlanType resolvedPlanType = request.getPlanType();
        String resolvedName = request.getName();
        Boolean resolvedActive = request.getIsActive();
        Boolean resolvedPopular = request.getIsPopular();

        if (role == null || role == Role.CANDIDATE) {
            resolvedTarget = PlanTarget.CANDIDATE;
            resolvedActive = true;
        } else if (role == Role.RECRUITER) {
            resolvedTarget = PlanTarget.COMPANY;
            resolvedActive = true;
        } else if (role != Role.ADMIN) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Plan> plans = planRepository.findAll(
                PlanSpecification.withFilter(resolvedName, resolvedTarget, resolvedPlanType, resolvedActive, resolvedPopular),
                pageable
        );

        Integer currentPlanId = resolveCurrentPlanId(role);
        return PagingResponse.fromPage(plans.map(plan -> buildResponse(plan, role, currentPlanId)));
    }

    private String normalizeCurrency(String currency) {
        return (currency == null || currency.isBlank()) ? "VND" : currency;
    }

    private boolean isUpdateOnlyDefault(Plan plan, PlanUpdateRequest request) {
        return Objects.equals(request.getName(), plan.getName())
                && Objects.equals(request.getDescription(), plan.getDescription())
                && Objects.equals(request.getPlanDetails(), plan.getPlanDetails())
                && Objects.equals(request.getPlanTarget(), plan.getPlanTarget())
                && Objects.equals(request.getPlanType(), plan.getPlanType())
                && Objects.equals(normalizeCurrency(request.getCurrency()), plan.getCurrency())
                && equalsIfPresent(request.getIsActive(), plan.getIsActive())
                && equalsIfPresent(request.getIsPopular(), plan.getIsPopular());
    }

    private boolean equalsIfPresent(Boolean incoming, Boolean current) {
        return incoming == null || Objects.equals(incoming, current);
    }


    private PlanResponse buildResponse(Plan plan, Role role, Integer currentPlanId) {
        PlanResponse response = planMapper.toResponse(plan);
        List<PlanPrice> prices = planPriceRepository.findAllByPlanId(plan.getId());
        List<PlanPriceResponse> priceResponses = planPriceMapper.toResponses(prices);
        Role resolvedRole = role == null ? JwtTokenProvider.getCurrentRole() : role;
        if (resolvedRole == null || resolvedRole == Role.CANDIDATE || resolvedRole == Role.RECRUITER) {
            priceResponses = priceResponses.stream()
                    .filter(pr -> Boolean.TRUE.equals(pr.getIsActive()))
                    .collect(Collectors.toList());
        }
        response.setPlanPrices(priceResponses);
        response.setIsCurrent(currentPlanId != null && Objects.equals(plan.getId(), currentPlanId));
        return response;
    }

    private Integer resolveCurrentPlanId(Role role) {
        if (role == null || (role != Role.CANDIDATE && role != Role.RECRUITER)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Subscription> subscriptions;
        if (role == Role.CANDIDATE) {
            subscriptions = subscriptionRepository.findEligibleByCandidateId(
                    JwtTokenProvider.getCurrentCandidateId(),
                    SubscriptionStatus.ACTIVE,
                    now
            );
        } else {
            Integer companyId = resolveCompanyId(JwtTokenProvider.getCurrentRecruiterId());
            if (companyId == null) {
                return null;
            }
            subscriptions = subscriptionRepository.findEligibleByCompanyId(
                    companyId,
                    SubscriptionStatus.ACTIVE,
                    now
            );
        }

        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }

        Subscription mainSubscription = subscriptions.stream()
                .filter(sub -> sub.getPlan() != null && sub.getPlan().getPlanType() == PlanType.MAIN)
                .findFirst()
                .orElse(subscriptions.get(0));

        return mainSubscription.getPlan() != null ? mainSubscription.getPlan().getId() : null;
    }

    private Integer resolveCompanyId(Integer recruiterId) {
        if (recruiterId == null) {
            return null;
        }
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        return recruiter.getCompany() != null ? recruiter.getCompany().getId() : null;
    }
}
