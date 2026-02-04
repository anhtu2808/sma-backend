package com.sma.core.mapper.company;

import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.entity.Company;
import com.sma.core.mapper.job.JobMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = {
                CompanyLocationMapper.class,
                CompanyImageMapper.class,
                JobMapper.class,
        })
public interface CompanyMapper {

    BaseCompanyResponse toBaseCompanyResponse(Company company);
    CompanyDetailResponse toCompanyDetailResponse(Company company);

}
