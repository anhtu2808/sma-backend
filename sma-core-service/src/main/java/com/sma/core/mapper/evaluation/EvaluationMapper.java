package com.sma.core.mapper.evaluation;

import com.sma.core.dto.message.suggest.ReSuggestRequestMessage;
import com.sma.core.dto.message.suggest.SuggestionRequestMessage;
import com.sma.core.dto.request.evaluation.suggest.GapSuggestionRequest;
import com.sma.core.dto.request.evaluation.suggest.WeaknessSuggestionRequest;
import com.sma.core.dto.response.evaluation.EvaluationWeaknessResponse;
import com.sma.core.dto.response.suggestion.GapSuggestionResponse;
import com.sma.core.entity.EvaluationGap;
import com.sma.core.entity.EvaluationWeakness;
import com.sma.core.entity.ResumeEvaluation;
import jdk.jshell.SourceCodeAnalysis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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

    @Mapping(target = "jobName", source = "job.name")
    @Mapping(target = "jobLevel", source = "job.jobLevel")
    @Mapping(target = "weakness", ignore = true)
    ReSuggestRequestMessage toReSuggestRequestMessage(ResumeEvaluation evaluation);
}
