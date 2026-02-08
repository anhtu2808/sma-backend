package com.sma.core.mapper.job;

import com.sma.core.dto.request.question.UpsertQuestionRequest;
import com.sma.core.dto.response.question.JobQuestionResponse;
import com.sma.core.entity.JobQuestion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface JobQuestionMapper {

    JobQuestionResponse toJobQuestionResponse(JobQuestion jobQuestion);

    JobQuestion toQuestion(UpsertQuestionRequest request);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestion(UpsertQuestionRequest request, @MappingTarget JobQuestion jobQuestion);

}
