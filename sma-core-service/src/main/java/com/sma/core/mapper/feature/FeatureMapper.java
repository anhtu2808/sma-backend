package com.sma.core.mapper.feature;

import com.sma.core.dto.request.feature.FeatureRequest;
import com.sma.core.dto.response.feature.FeatureResponse;
import com.sma.core.entity.Feature;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeatureMapper {
    FeatureResponse toResponse(Feature feature);

    List<FeatureResponse> toResponses(List<Feature> features);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "featureKey", expression = "java(request.getFeatureKey().name())")
    Feature toEntity(FeatureRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "featureKey", expression = "java(request.getFeatureKey() == null ? null : request.getFeatureKey().name())")
    void updateFromRequest(FeatureRequest request, @MappingTarget Feature feature);
}
