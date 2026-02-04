package com.sma.core.mapper.company;

import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.entity.Company;
import com.sma.core.mapper.job.JobMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {
                CompanyLocationMapper.class,
                CompanyImageMapper.class,
                JobMapper.class,
        })
public interface CompanyMapper {

    @Mapping(target = "companyStatus", source = "status")
    BaseCompanyResponse toBaseCompanyResponse(Company company);

    @Mapping(target = "jobs", qualifiedByName = "baseJob")
    @Mapping(target = "companyStatus", source = "status")
    CompanyDetailResponse toCompanyDetailResponse(Company company);

}
