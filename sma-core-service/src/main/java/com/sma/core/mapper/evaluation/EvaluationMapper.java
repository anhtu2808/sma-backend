package com.sma.core.mapper.evaluation;

import com.sma.core.dto.message.suggest.ReSuggestRequestMessage;
import com.sma.core.dto.message.suggest.SuggestionRequestMessage;
import com.sma.core.dto.request.evaluation.suggest.GapSuggestionRequest;
import com.sma.core.dto.request.evaluation.suggest.WeaknessSuggestionRequest;
import com.sma.core.entity.EvaluationCriteriaSuggestion;
import com.sma.core.entity.EvaluationGap;
import com.sma.core.entity.EvaluationWeakness;
import com.sma.core.entity.ResumeEvaluation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationMapper {

    @Mapping(target = "jobName", source = "job.name")
    @Mapping(target = "jobLevel", source = "job.jobLevel")
    @Mapping(target = "evaluationId", source = "id")
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "resumeId", source = "resume.id")
    SuggestionRequestMessage toSuggestionRequestMessage(ResumeEvaluation evaluation);

    GapSuggestionRequest toGapSuggestionRequest(EvaluationGap gap);
    WeaknessSuggestionRequest toWeaknessSuggestionRequest(EvaluationWeakness weakness);

    @Mapping(target = "suggestionId", source = "id")
    @Mapping(target = "label", source = "evaluationCriteriaDetail.label")
    @Mapping(target = "context", source = "evaluationCriteriaDetail.context")
    @Mapping(target = "description", source = "evaluationCriteriaDetail.description")
    @Mapping(target = "aiExplanation", source = "evaluationCriteriaDetail.evaluationCriteriaScore.aiExplanation")
    @Mapping(target = "rule", source = "evaluationCriteriaDetail.evaluationCriteriaScore.scoringCriteria.rule")
    @Mapping(target = "scoringCriteriaContext", source = "evaluationCriteriaDetail.evaluationCriteriaScore.scoringCriteria.context")
    @Mapping(target = "summary", source = "evaluationCriteriaDetail.evaluationCriteriaScore.evaluation.summary")
    @Mapping(target = "weakness", source = "evaluationCriteriaDetail.evaluationCriteriaScore.evaluation.weakness")
    ReSuggestRequestMessage toReSuggestRequestMessage(EvaluationCriteriaSuggestion suggestion);
}
