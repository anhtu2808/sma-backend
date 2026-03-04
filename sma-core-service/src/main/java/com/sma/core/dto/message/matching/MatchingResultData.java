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
    List<GapData> gaps;
    List<WeaknessData> weaknesses;

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
        List<HardSkillData> hardSkills;
        List<SoftSkillData> softSkills;
        List<ExperienceDetailData> experienceDetails;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class HardSkillData {
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SoftSkillData {
        String skillName;
        String evidence;
        Boolean isRequired;
        Boolean isFound;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExperienceDetailData {
        String companyName;
        String position;
        Integer durationMonths;
        String keyAchievements;
        String technologiesUsed;
        Boolean isRelevant;
        RelevanceType transferabilityToRole;
        RelevanceType experienceGravity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GapData {
        GapType gapType;
        String itemName;
        String description;
        ImpactType impact;
        Float impactScore;
        String suggestion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WeaknessData {
        String weaknessText;
        String suggestion;
        Integer startIndex;
        Integer endIndex;
        String context;
        CriteriaType criterionType;
        Short severity;
    }
}
