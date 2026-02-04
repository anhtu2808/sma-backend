package com.sma.core.mapper;

import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobResponse;
import com.sma.core.dto.response.job.PublicJobResponse;
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

    BaseJobResponse toBaseJobResponse(Job job);
    PublicJobResponse toPublicJobResponse(Job job);
    JobResponse toJobResponse(Job job);

}
