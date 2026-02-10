package com.sma.core.service.impl;

import com.sma.core.dto.request.planprice.PlanPriceRequest;
import com.sma.core.dto.response.planprice.PlanPriceResponse;
import com.sma.core.entity.Plan;
import com.sma.core.entity.PlanPrice;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.plan.PlanPriceMapper;
import com.sma.core.repository.PlanPriceRepository;
import com.sma.core.repository.PlanRepository;
import com.sma.core.service.PlanPriceService;
import com.sma.core.enums.Role;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PlanPriceServiceImpl implements PlanPriceService {

    PlanRepository planRepository;
    PlanPriceRepository planPriceRepository;
    PlanPriceMapper planPriceMapper;

    @Override
    public PlanPriceResponse addPrice(Integer planId, PlanPriceRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        PlanPrice price = planPriceMapper.toEntity(request);
        price.setPlan(plan);
        if (price.getCurrency() == null || price.getCurrency().isBlank()) {
            price.setCurrency(plan.getCurrency());
        }
        if (price.getIsActive() == null) {
            price.setIsActive(true);
        }

        price = planPriceRepository.save(price);
        return planPriceMapper.toResponse(price);
    }

    @Override
    public void deletePrice(Integer planId, Integer priceId) {
        PlanPrice price = planPriceRepository.findById(priceId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_PRICE_NOT_FOUND));
        if (!price.getPlan().getId().equals(planId)) {
            throw new AppException(ErrorCode.PLAN_PRICE_NOT_FOUND);
        }
        planPriceRepository.delete(price);
    }

    @Override
    public PlanPriceResponse updatePrice(Integer planId, Integer priceId, PlanPriceRequest request) {
        PlanPrice price = planPriceRepository.findById(priceId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_PRICE_NOT_FOUND));
        if (!price.getPlan().getId().equals(planId)) {
            throw new AppException(ErrorCode.PLAN_PRICE_NOT_FOUND);
        }

        planPriceMapper.updateFromRequest(request, price);
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            price.setCurrency(price.getPlan().getCurrency());
        }

        PlanPrice saved = planPriceRepository.save(price);

        return planPriceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanPriceResponse> getPrices(Integer planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        List<PlanPriceResponse> responses = planPriceMapper.toResponses(
                planPriceRepository.findAllByPlanId(plan.getId())
        );
        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null || role == Role.CANDIDATE || role == Role.RECRUITER) {
            return responses.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                    .collect(Collectors.toList());
        }
        return responses;
    }
}
