package com.sma.core.dto.response.resume;

import com.sma.core.enums.RelevanceType;
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
public class EvaluationExperienceDetailResponse {
    Integer id;
    String companyName;
    String position;
    Integer durationMonths;
    String keyAchievements;
    String technologiesUsed;
    Boolean isRelevant;
    RelevanceType transferabilityToRole;
    RelevanceType experienceGravity;
}
