package com.sma.core.mapper.job;

import com.sma.core.dto.response.job.JobQuestionResponse;
import com.sma.core.entity.JobQuestion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobQuestionMapper {

    JobQuestionResponse toJobQuestionResponse(JobQuestion jobQuestion);

}
