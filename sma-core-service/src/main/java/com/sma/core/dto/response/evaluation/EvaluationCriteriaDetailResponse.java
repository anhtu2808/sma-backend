package com.sma.core.dto.response.evaluation;

import com.sma.core.enums.LabelStatus;
import com.sma.core.enums.SkillLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvaluationCriteriaDetailResponse {

    Integer id;
    String label;
    LabelStatus status;
    String description;
    SkillLevel requiredLevel;
    SkillLevel candidateLevel;
    Boolean isRequired;
    Boolean isFixed;
    String context;
    Float impactScore;
    Set<String> suggestions;
}
