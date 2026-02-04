package com.sma.core.mapper.company;

import com.sma.core.dto.response.company.CompanyLocationResponse;
import com.sma.core.entity.CompanyLocation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyLocationMapper {

    CompanyLocationResponse toResponse(CompanyLocation companyLocation);

}
