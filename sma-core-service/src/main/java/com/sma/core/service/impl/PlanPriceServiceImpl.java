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
import com.sma.core.repository.UsageLimitRepository;
import com.sma.core.service.PlanPriceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PlanPriceServiceImpl implements PlanPriceService {

    PlanRepository planRepository;
    PlanPriceRepository planPriceRepository;
    UsageLimitRepository usageLimitRepository;
    PlanPriceMapper planPriceMapper;

    @Override
    public PlanPriceResponse addPrice(Integer planId, PlanPriceRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        PlanPrice price = PlanPrice.builder()
                .plan(plan)
                .originalPrice(request.getOriginalPrice())
                .salePrice(request.getSalePrice())
                .currency(request.getCurrency() == null || request.getCurrency().isBlank() ? plan.getCurrency() : request.getCurrency())
                .duration(request.getDuration())
                .unit(request.getUnit())
                .isActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive())
                .build();

        price = planPriceRepository.save(price);

        if (Boolean.FALSE.equals(plan.getIsActive())) {
            boolean hasLimit = usageLimitRepository.existsByPlanId(plan.getId());
            if (hasLimit) {
                plan.setIsActive(true);
                planRepository.save(plan);
            }
        }

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
}
