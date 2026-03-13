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

    MatchLevel matchLevel;
    String summary;
    String strengths;
    String weakness;
    Boolean isTrueLevel;
    Boolean hasRelatedExperience;
    RelevanceType transferabilityToRole;
    Float processingTimeSecond;
    String aiModelVersion;
    List<CriteriaScoreData> criteriaScores;

}
