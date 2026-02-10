package com.sma.core.mapper.feature;

import com.sma.core.dto.response.feature.FeatureResponse;
import com.sma.core.entity.Feature;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeatureMapper {
    FeatureResponse toResponse(Feature feature);

    List<FeatureResponse> toResponses(List<Feature> features);
}
