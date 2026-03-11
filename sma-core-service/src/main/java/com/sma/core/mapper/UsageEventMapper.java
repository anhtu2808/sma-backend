package com.sma.core.mapper;

import com.sma.core.dto.response.usage.UsageEventContextResponse;
import com.sma.core.dto.response.usage.UsageEventResponse;
import com.sma.core.entity.UsageEvent;
import com.sma.core.entity.UsageEventContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UsageEventMapper {

    @Mapping(target = "contexts", expression = "java(toContextResponses(entity.getContexts()))")
    @Mapping(source = "feature.featureKey", target = "featureKey")
    @Mapping(source = "feature.name", target = "featureName")
    @Mapping(source = "subscription.plan.name", target = "planName")
    UsageEventResponse toResponse(UsageEvent entity);

    default List<UsageEventContextResponse> toContextResponses(Set<UsageEventContext> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return List.of();
        }

        return contexts.stream()
                .sorted(Comparator
                        .comparing((UsageEventContext context) -> context.getEventSource().name())
                        .thenComparing(UsageEventContext::getSourceId))
                .map(this::toContextResponse)
                .toList();
    }

    default UsageEventContextResponse toContextResponse(UsageEventContext context) {
        return UsageEventContextResponse.builder()
                .eventSource(context.getEventSource())
                .sourceId(context.getSourceId())
                .build();
    }
}
