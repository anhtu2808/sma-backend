package com.sma.core.mapper.plan;

import com.sma.core.dto.request.plan.PlanCreateRequest;
import com.sma.core.dto.request.plan.PlanUpdateRequest;
import com.sma.core.dto.response.plan.PlanResponse;
import com.sma.core.dto.response.usagelimit.UsageLimitResponse;
import com.sma.core.entity.Plan;
import com.sma.core.entity.UsageLimit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    PlanResponse toResponse(Plan plan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "planPrices", ignore = true)
    @Mapping(target = "usageLimits", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    Plan toEntity(PlanCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "planPrices", ignore = true)
    @Mapping(target = "usageLimits", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    void updateFromRequest(PlanUpdateRequest request, @MappingTarget Plan plan);

    @Mapping(target = "featureId", source = "feature.id")
    @Mapping(target = "featureKey", source = "feature.featureKey")
    @Mapping(target = "featureName", source = "feature.name")
    UsageLimitResponse toUsageLimitResponse(UsageLimit usageLimit);
}
