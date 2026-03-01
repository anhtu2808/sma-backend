package com.sma.core.mapper;

import com.sma.core.dto.response.usage.UsageEventResponse;
import com.sma.core.entity.UsageEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsageEventMapper {

    @Mapping(source = "eventSource", target = "eventSource")
    @Mapping(source = "sourceId", target = "entityId")
    @Mapping(source = "feature.featureKey", target = "featureKey")
    @Mapping(source = "feature.name", target = "featureName")
    UsageEventResponse toResponse(UsageEvent entity);
}
