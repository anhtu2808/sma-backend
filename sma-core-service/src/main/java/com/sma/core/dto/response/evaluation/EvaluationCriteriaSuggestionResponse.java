package com.sma.core.dto.response.evaluation;

import com.sma.core.enums.LabelStatus;
import com.sma.core.enums.SkillLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvaluationCriteriaSuggestionResponse {

    Integer id;
    String label;
    LabelStatus status;
    SkillLevel requiredLevel;
    SkillLevel candidateLevel;
    Boolean isRequired;
    Integer startIndex;
    Integer endIndex;
    Float impactScore;
    List<String> suggestions;
}
