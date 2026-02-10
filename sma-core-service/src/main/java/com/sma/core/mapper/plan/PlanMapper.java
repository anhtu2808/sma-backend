package com.sma.core.mapper.plan;

import com.sma.core.dto.response.plan.PlanResponse;
import com.sma.core.dto.response.usagelimit.UsageLimitResponse;
import com.sma.core.entity.Plan;
import com.sma.core.entity.UsageLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    PlanResponse toResponse(Plan plan);

    @Mapping(target = "featureId", source = "feature.id")
    @Mapping(target = "featureKey", source = "feature.featureKey")
    @Mapping(target = "featureName", source = "feature.name")
    UsageLimitResponse toUsageLimitResponse(UsageLimit usageLimit);
}
