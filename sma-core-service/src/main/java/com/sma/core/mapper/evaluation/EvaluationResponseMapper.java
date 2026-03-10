package com.sma.core.mapper.evaluation;

import com.sma.core.dto.response.evaluation.*;
import com.sma.core.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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

    @Named("detailCriteriaScores")
    default java.util.List<EvaluationCriteriaScoreResponse> toDetailCriteriaScores(java.util.Set<EvaluationCriteriaScore> scores) {
        if (scores == null) return null;
        return scores.stream().map(this::toCriteriaScoreResponse).toList();
    }

    @Mapping(target = "scoringCriteriaId", source = "scoringCriteria.id")
    @Mapping(target = "scoringCriteriaContext", source = "scoringCriteria.context")
    @Mapping(target = "scoringCriteriaWeight", source = "scoringCriteria.weight")
    @Mapping(target = "criteriaType", source = "scoringCriteria.criteria.criteriaType")
    @Mapping(target = "details", ignore = true)
    EvaluationCriteriaScoreResponse toCriteriaScoreResponse(EvaluationCriteriaScore criteriaScore);

    // --- Overview Response ---
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobName", source = "job.name")
    @Mapping(target = "resumeId", source = "resume.id")
    @Mapping(target = "resumeFullName", source = "resume.fullName")
    @Mapping(target = "candidateName", source = "resume.candidate.user.fullName")
    @Named("toOverviewResponse")
    ResumeEvaluationOverviewResponse toOverviewResponse(ResumeEvaluation evaluation);


}
