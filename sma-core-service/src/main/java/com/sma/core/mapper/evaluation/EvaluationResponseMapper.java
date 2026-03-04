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
    @Mapping(target = "criteriaScores", source = "criteriaScores", qualifiedByName = "detailCriteriaScores")
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
    @Mapping(target = "criteriaId", source = "scoringCriteria.criteria.id")
    @Mapping(target = "criteriaName", source = "scoringCriteria.criteria.name")
    @Mapping(target = "criteriaType", source = "scoringCriteria.criteria.criteriaType")
    EvaluationCriteriaScoreResponse toCriteriaScoreResponse(EvaluationCriteriaScore criteriaScore);

    EvaluationHardSkillResponse toHardSkillResponse(EvaluationHardSkill hardSkill);

    EvaluationSoftSkillResponse toSoftSkillResponse(EvaluationSoftSkill softSkill);

    EvaluationExperienceDetailResponse toExperienceDetailResponse(EvaluationExperienceDetail detail);

    EvaluationGapResponse toGapResponse(EvaluationGap gap);

    EvaluationWeaknessResponse toWeaknessResponse(EvaluationWeakness weakness);

    // --- Overview Response ---
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobName", source = "job.name")
    @Mapping(target = "resumeId", source = "resume.id")
    @Mapping(target = "resumeFullName", source = "resume.fullName")
    @Mapping(target = "candidateName", source = "resume.candidate.user.fullName")
    @Mapping(target = "criteriaScores", source = "criteriaScores", qualifiedByName = "overviewCriteriaScores")
    @Named("toOverviewResponse")
    ResumeEvaluationOverviewResponse toOverviewResponse(ResumeEvaluation evaluation);

    @Named("overviewCriteriaScores")
    default java.util.List<EvaluationCriteriaScoreResponse> toOverviewCriteriaScores(java.util.Set<EvaluationCriteriaScore> scores) {
        if (scores == null) return null;
        return scores.stream().map(this::toOverviewCriteriaScore).toList();
    }

    @Mapping(target = "scoringCriteriaId", source = "scoringCriteria.id")
    @Mapping(target = "scoringCriteriaContext", ignore = true)
    @Mapping(target = "scoringCriteriaWeight", source = "scoringCriteria.weight")
    @Mapping(target = "criteriaId", source = "scoringCriteria.criteria.id")
    @Mapping(target = "criteriaName", source = "scoringCriteria.criteria.name")
    @Mapping(target = "criteriaType", source = "scoringCriteria.criteria.criteriaType")
    @Mapping(target = "aiExplanation", ignore = true)
    @Mapping(target = "manualExplanation", ignore = true)
    @Mapping(target = "hardSkills", ignore = true)
    @Mapping(target = "softSkills", ignore = true)
    @Mapping(target = "experienceDetails", ignore = true)
    EvaluationCriteriaScoreResponse toOverviewCriteriaScore(EvaluationCriteriaScore criteriaScore);
}
