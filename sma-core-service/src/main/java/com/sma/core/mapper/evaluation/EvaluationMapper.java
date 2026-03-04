package com.sma.core.mapper.evaluation;

import com.sma.core.dto.message.suggest.SuggestionRequestMessage;
import com.sma.core.dto.request.evaluation.suggest.GapSuggestionRequest;
import com.sma.core.dto.request.evaluation.suggest.WeaknessSuggestionRequest;
import com.sma.core.entity.EvaluationGap;
import com.sma.core.entity.EvaluationWeakness;
import com.sma.core.entity.ResumeEvaluation;
import jdk.jshell.SourceCodeAnalysis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationMapper {

    @Mapping(target = "jobName", source = "job.name")
    @Mapping(target = "jobLevel", source = "job.jobLevel")
    SuggestionRequestMessage toSuggestionRequestMessage(ResumeEvaluation evaluation);

    GapSuggestionRequest toGapSuggestionRequest(EvaluationGap gap);
    WeaknessSuggestionRequest toWeaknessSuggestionRequest(EvaluationWeakness weakness);
}
