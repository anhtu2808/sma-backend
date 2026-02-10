package com.sma.core.mapper.plan;

import com.sma.core.dto.request.planprice.PlanPriceRequest;
import com.sma.core.dto.response.planprice.PlanPriceResponse;
import com.sma.core.entity.PlanPrice;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanPriceMapper {
    PlanPriceResponse toResponse(PlanPrice price);

    List<PlanPriceResponse> toResponses(List<PlanPrice> prices);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plan", ignore = true)
    PlanPrice toEntity(PlanPriceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plan", ignore = true)
    void updateFromRequest(PlanPriceRequest request, @MappingTarget PlanPrice price);
}
