package com.sma.core.mapper;

import com.sma.core.dto.response.job.JobResponse;
import com.sma.core.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses =
                {
                        CompanyMapper.class,
                        DomainMapper.class,
                        SkillMapper.class,
                        BenefitMapper.class
                })
public interface JobMapper {

    @Mapping(target = "about", ignore = true)
    @Mapping(target = "responsibilities", ignore = true)
    @Mapping(target = "requirement", ignore = true)
    @Mapping(target = "company", ignore = true)
    JobResponse toOverallJobResponse(Job job);

    JobResponse toJobResponse(Job job);

}
