package com.sma.core.mapper;

import com.sma.core.dto.response.company.CompanyResponse;
import com.sma.core.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);

}
