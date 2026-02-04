package com.sma.core.mapper.job;

import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.entity.Job;
import com.sma.core.mapper.SkillMapper;
import com.sma.core.mapper.company.CompanyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring",
        uses = {
                CompanyMapper.class,
                DomainMapper.class,
                SkillMapper.class,
                BenefitMapper.class
        })
public interface JobMapper {

    @Named("baseJob")
    BaseJobResponse toBaseJobResponse(Job job);

    @Mapping(target = "rootJob", ignore = true)
    @Named("clientJobDetail")
    JobDetailResponse toJobDetailResponse(Job job);

    @Named("fullJobDetail")
    JobDetailResponse toJobInternalResponse(Job job);

}
