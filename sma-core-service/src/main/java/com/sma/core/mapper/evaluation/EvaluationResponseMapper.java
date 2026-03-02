package com.sma.core.mapper.evaluation;

import com.sma.core.dto.response.resume.*;
import com.sma.core.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationResponseMapper {

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobName", source = "job.name")
    ResumeEvaluationResponse toEvaluationResponse(ResumeEvaluation evaluation);

    @Mapping(target = "scoringCriteriaId", source = "scoringCriteria.id")
    @Mapping(target = "scoringCriteriaContext", source = "scoringCriteria.context")
    @Mapping(target = "scoringCriteriaWeight", source = "scoringCriteria.weight")
    @Mapping(target = "criteriaId", source = "scoringCriteria.criteria.id")
    @Mapping(target = "criteriaName", source = "scoringCriteria.criteria.name")
    @Mapping(target = "criteriaType", source = "scoringCriteria.criteria.criteriaType")
    EvaluationCriteriaScoreResponse toCriteriaScoreResponse(EvaluationCriteriaScore criteriaScore);

    EvaluationHardSkillResponse toHardSkillResponse(EvaluationHardSkill hardSkill);

    EvaluationSoftSkillResponse toSoftSkillResponse(EvaluationSoftSkill softSkill);

    EvaluationExperienceDetailResponse toExperienceDetailResponse(EvaluationExperienceDetail detail);

    EvaluationGapResponse toGapResponse(EvaluationGap gap);

    EvaluationWeaknessResponse toWeaknessResponse(EvaluationWeakness weakness);
}
