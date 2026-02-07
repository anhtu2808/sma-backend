package com.sma.core.dto.response.resume;

import com.sma.core.enums.RelevanceType;
import com.sma.core.enums.SkillCategory;
import com.sma.core.enums.SkillLevel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvaluationHardSkillResponse {
    Integer id;
    String skillName;
    String evidence;
    SkillCategory skillCategory;
    SkillLevel requiredLevel;
    SkillLevel candidateLevel;
    Float matchScore;
    Float yearsOfExperience;
    Boolean isCritical;
    Boolean isMatched;
    Boolean isMissing;
    Boolean isExtra;
    RelevanceType relevance;
}
