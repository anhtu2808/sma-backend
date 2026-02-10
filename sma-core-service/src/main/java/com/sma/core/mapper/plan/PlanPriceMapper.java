package com.sma.core.mapper.plan;

import com.sma.core.dto.response.planprice.PlanPriceResponse;
import com.sma.core.entity.PlanPrice;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanPriceMapper {
    PlanPriceResponse toResponse(PlanPrice price);

    List<PlanPriceResponse> toResponses(List<PlanPrice> prices);
}
