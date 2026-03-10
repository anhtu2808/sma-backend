package com.sma.core.dto.message.matching;

import com.sma.core.enums.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingResultData {

    Float aiOverallScore;
    MatchLevel matchLevel;
    String summary;
    String strengths;
    String weakness;
    Boolean isTrueLevel;
    Boolean hasRelatedExperience;
    Boolean isSpecificJd;
    Float processingTimeSecond;
    String aiModelVersion;
    List<CriteriaScoreData> criteriaScores;

    // ---- Nested DTOs ----

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CriteriaScoreData {
        CriteriaType criteriaType;
        Float aiScore;
        Float maxScore;
        Float weightedScore;
        String aiExplanation;
    }


}
