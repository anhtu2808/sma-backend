package com.sma.core.dto.message.matching;

import com.sma.core.enums.LabelStatus;
import com.sma.core.enums.SkillLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaScoreDetailData {

    String label;
    LabelStatus status;
    String description;
    SkillLevel requiredLevel;
    SkillLevel candidateLevel;
    Integer startIndex;
    Integer endIndex;
    Float impactScore;
    List<String> suggestions;
}
