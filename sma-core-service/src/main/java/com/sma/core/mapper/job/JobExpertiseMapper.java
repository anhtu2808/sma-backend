package com.sma.core.mapper.job;

import com.sma.core.dto.response.job.JobExpertiseResponse;
import com.sma.core.entity.JobExpertise;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobExpertiseMapper {

    JobExpertiseResponse toResponse(JobExpertise jobExpertise);

}
