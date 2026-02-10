package com.sma.core.service.impl;

import com.sma.core.dto.request.plan.PlanCreateRequest;
import com.sma.core.dto.request.plan.PlanUpdateRequest;
import com.sma.core.dto.request.plan.PlanFilterRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.plan.PlanResponse;
import com.sma.core.dto.response.planprice.PlanPriceResponse;
import com.sma.core.entity.Plan;
import com.sma.core.entity.PlanPrice;
import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.plan.PlanMapper;
import com.sma.core.mapper.plan.PlanPriceMapper;
import com.sma.core.repository.PlanPriceRepository;
import com.sma.core.repository.PlanRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PlanServiceImpl implements PlanService {

    PlanRepository planRepository;
    PlanPriceRepository planPriceRepository;
    SubscriptionRepository subscriptionRepository;
    PlanMapper planMapper;
    PlanPriceMapper planPriceMapper;

    @Override
    public PlanResponse createPlan(PlanCreateRequest request) {
        if (planRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.PLAN_ALREADY_EXISTS);
        }

        Plan plan = Plan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .planTarget(request.getPlanTarget())
                .planType(request.getPlanType())
                .currency(normalizeCurrency(request.getCurrency()))
                .isActive(false)
                .build();

        plan = planRepository.save(plan);

        return buildResponse(plan);
    }

    @Override
    public PlanResponse updatePlan(Integer id, PlanUpdateRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        boolean hasSubscription = subscriptionRepository.existsByPlanId(id);

        if (hasSubscription) {
            throw new AppException(ErrorCode.PLAN_UPDATE_ONLY_PRICE_ALLOWED);
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPlanTarget(request.getPlanTarget());
        plan.setPlanType(request.getPlanType());
        plan.setCurrency(normalizeCurrency(request.getCurrency()));

        planRepository.save(plan);
        return buildResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse getPlanById(Integer id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        return buildResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<PlanResponse> getPlans(PlanFilterRequest request) {
        Role role = JwtTokenProvider.getCurrentRole();
        PlanTarget resolvedTarget = request.getPlanTarget();
        PlanType resolvedPlanType = request.getPlanType();
        String resolvedName = request.getName();
        Boolean resolvedActive = request.getIsActive();

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
                PlanSpecification.withFilter(resolvedName, resolvedTarget, resolvedPlanType, resolvedActive),
                pageable
        );

        return PagingResponse.fromPage(plans.map(this::buildResponse));
    }

    private String normalizeCurrency(String currency) {
        return (currency == null || currency.isBlank()) ? "VND" : currency;
    }


    private PlanResponse buildResponse(Plan plan) {
        PlanResponse response = planMapper.toResponse(plan);
        List<PlanPrice> prices = planPriceRepository.findAllByPlanId(plan.getId());
        List<PlanPriceResponse> priceResponses = planPriceMapper.toResponses(prices);
        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null || role == Role.CANDIDATE || role == Role.RECRUITER) {
            priceResponses = priceResponses.stream()
                    .filter(pr -> Boolean.TRUE.equals(pr.getIsActive()))
                    .collect(Collectors.toList());
        }
        response.setPlanPrices(priceResponses);
        return response;
    }
}
