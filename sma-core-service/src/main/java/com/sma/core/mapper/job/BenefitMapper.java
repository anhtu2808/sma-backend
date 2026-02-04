package com.sma.core.mapper.job;

import com.sma.core.dto.response.job.BenefitResponse;
import com.sma.core.entity.Benefit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BenefitMapper {

    BenefitResponse toResponse(Benefit benefit);

}
