package com.sma.core.service.impl;

import com.sma.core.dto.request.usagelimit.UsageLimitRequest;
import com.sma.core.dto.response.usagelimit.UsageLimitResponse;
import com.sma.core.entity.Feature;
import com.sma.core.entity.Plan;
import com.sma.core.entity.UsageLimit;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.plan.UsageLimitMapper;
import com.sma.core.repository.FeatureRepository;
import com.sma.core.repository.PlanRepository;
import com.sma.core.repository.SubscriptionRepository;
import com.sma.core.repository.UsageLimitRepository;
import com.sma.core.service.UsageLimitService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class UsageLimitServiceImpl implements UsageLimitService {

    PlanRepository planRepository;
    FeatureRepository featureRepository;
    UsageLimitRepository usageLimitRepository;
    SubscriptionRepository subscriptionRepository;
    UsageLimitMapper usageLimitMapper;

    @Override
    public UsageLimitResponse addLimit(Integer planId, UsageLimitRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        if (subscriptionRepository.existsByPlanId(planId)) {
            throw new AppException(ErrorCode.PLAN_UPDATE_ONLY_PRICE_ALLOWED);
        }
        if (usageLimitRepository.existsByPlanIdAndFeatureId(planId, request.getFeatureId())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Feature feature = featureRepository.findById(request.getFeatureId())
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));

        UsageLimit limit = usageLimitMapper.toEntity(request);
        limit.setPlan(plan);
        limit.setFeature(feature);

        return usageLimitMapper.toResponse(usageLimitRepository.save(limit));
    }

    @Override
    public UsageLimitResponse updateLimit(Integer planId, Integer featureId, UsageLimitRequest request) {
        if (subscriptionRepository.existsByPlanId(planId)) {
            throw new AppException(ErrorCode.PLAN_UPDATE_ONLY_PRICE_ALLOWED);
        }
        UsageLimit limit = usageLimitRepository.findByPlanIdAndFeatureId(planId, featureId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!featureId.equals(request.getFeatureId())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        usageLimitMapper.updateFromRequest(request, limit);
        return usageLimitMapper.toResponse(usageLimitRepository.save(limit));
    }

    @Override
    public void deleteLimit(Integer planId, Integer featureId) {
        if (subscriptionRepository.existsByPlanId(planId)) {
            throw new AppException(ErrorCode.PLAN_UPDATE_ONLY_PRICE_ALLOWED);
        }
        UsageLimit limit = usageLimitRepository.findByPlanIdAndFeatureId(planId, featureId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        usageLimitRepository.delete(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsageLimitResponse> getLimits(Integer planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        List<UsageLimit> limits = usageLimitRepository.findAllByPlanId(plan.getId());
        return usageLimitMapper.toResponses(limits);
    }
}
