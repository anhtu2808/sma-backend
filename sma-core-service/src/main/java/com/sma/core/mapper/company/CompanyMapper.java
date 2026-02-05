package com.sma.core.mapper.company;

import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.entity.Company;
import com.sma.core.mapper.job.JobMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring",
        uses = {
                CompanyLocationMapper.class,
                CompanyImageMapper.class,
                JobMapper.class,
        })
public interface CompanyMapper {

    @Mapping(target = "companyStatus", source = "status")
    @Named("baseCompany")
    BaseCompanyResponse toBaseCompanyResponse(Company company);

    @Mapping(target = "companyStatus", source = "status")
    @Named("clientCompanyDetail")
    CompanyDetailResponse toCompanyDetailResponse(Company company);

    @Mapping(target = "companyStatus", source = "status")
    @Named("fullCompanyDetail")
    CompanyDetailResponse toInternalCompanyResponse(Company company);

}
