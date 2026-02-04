package com.sma.core.mapper.job;

import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobInternalResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.entity.Job;
import com.sma.core.mapper.SkillMapper;
import com.sma.core.mapper.company.CompanyMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = {
                CompanyMapper.class,
                DomainMapper.class,
                SkillMapper.class,
                BenefitMapper.class
        })
public interface JobMapper {

    BaseJobResponse toBaseJobResponse(Job job);
    JobDetailResponse toJobDetailResponse(Job job);
    JobInternalResponse toJobInternalResponse(Job job);

}
