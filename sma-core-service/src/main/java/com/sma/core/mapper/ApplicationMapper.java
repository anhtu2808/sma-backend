package com.sma.core.mapper;

import com.sma.core.dto.response.application.ApplicationResponse;
import com.sma.core.dto.response.application.JobAnswerResponse;
import com.sma.core.entity.Application;
import com.sma.core.entity.JobAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.name")
    @Mapping(target = "resumeId", source = "resume.id")
    @Mapping(target = "resumeName", source = "resume.resumeName")
    @Mapping(target = "answers", source = "answers")
    ApplicationResponse toResponse(Application application);

    @Mapping(target = "questionId", source = "jobQuestion.id")
    @Mapping(target = "questionText", source = "name")
    @Mapping(target = "answerContent", source = "answer")
    JobAnswerResponse toJobAnswerResponse(JobAnswer jobAnswer);

    List<ApplicationResponse> toResponseList(List<Application> applications);
}
