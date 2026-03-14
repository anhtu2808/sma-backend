package com.sma.core.mapper.evaluation;

import com.sma.core.dto.response.evaluation.*;
import com.sma.core.entity.*;
import jdk.jshell.SourceCodeAnalysis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EvaluationResponseMapper {

    // --- Detail Response ---
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobName", source = "job.name")
    @Mapping(target = "resumeId", source = "resume.id")
    @Mapping(target = "resumeFullName", source = "resume.fullName")
    @Mapping(target = "candidateName", source = "resume.candidate.user.fullName")
    @Named("toDetailResponse")
    ResumeEvaluationDetailResponse toDetailResponse(ResumeEvaluation evaluation);

    SuggestionResponse toSuggestionResponse(EvaluationCriteriaSuggestion suggestion);

    @Mapping(target = "scoringCriteriaId", source = "scoringCriteria.id")
    @Mapping(target = "scoringCriteriaContext", source = "scoringCriteria.context")
    @Mapping(target = "scoringCriteriaWeight", source = "scoringCriteria.weight")
    @Mapping(target = "criteriaName", source = "scoringCriteria.criteria.name")
    EvaluationCriteriaScoreResponse toCriteriaScoreResponse(EvaluationCriteriaScore criteriaScore);

    EvaluationCriteriaDetailResponse toDetailCriteriaScoreResponse(EvaluationCriteriaDetail criteriaScore);

    // --- Overview Response ---
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobName", source = "job.name")
    @Mapping(target = "resumeId", source = "resume.id")
    @Mapping(target = "resumeFullName", source = "resume.fullName")
    @Mapping(target = "candidateName", source = "resume.candidate.user.fullName")
    @Named("toOverviewResponse")
    ResumeEvaluationOverviewResponse toOverviewResponse(ResumeEvaluation evaluation);


}
