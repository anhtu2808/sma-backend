package com.sma.core.mapper.plan;

import com.sma.core.dto.request.usagelimit.UsageLimitRequest;
import com.sma.core.dto.response.usagelimit.UsageLimitResponse;
import com.sma.core.entity.UsageLimit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsageLimitMapper {
    @Mapping(target = "featureId", source = "feature.id")
    @Mapping(target = "featureKey", source = "feature.featureKey")
    @Mapping(target = "featureName", source = "feature.name")
    UsageLimitResponse toResponse(UsageLimit usageLimit);

    List<UsageLimitResponse> toResponses(List<UsageLimit> usageLimits);

    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "feature", ignore = true)
    UsageLimit toEntity(UsageLimitRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "feature", ignore = true)
    void updateFromRequest(UsageLimitRequest request, @MappingTarget UsageLimit usageLimit);
}
