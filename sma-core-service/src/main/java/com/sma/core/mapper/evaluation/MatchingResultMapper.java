package com.sma.core.mapper.evaluation;

import com.sma.core.dto.message.matching.CriteriaScoreData;
import com.sma.core.dto.message.matching.MatchingResultData;
import com.sma.core.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MatchingResultMapper {

    // ---- Top-level evaluation mapping ----

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "evaluationStatus", constant = "FINISH")
    @Mapping(target = "evaluationType", ignore = true)
    @Mapping(target = "recruiterOverallScore", ignore = true)
    @Mapping(target = "criteriaScores", ignore = true)
    @Mapping(target = "gaps", ignore = true)
    @Mapping(target = "weaknesses", ignore = true)
    void mapToEvaluation(MatchingResultData data, @MappingTarget ResumeEvaluation evaluation);

    // ---- Criteria score mapping ----

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "details", ignore = true)
    EvaluationCriteriaScore toCriteriaScore(CriteriaScoreData data);

}
