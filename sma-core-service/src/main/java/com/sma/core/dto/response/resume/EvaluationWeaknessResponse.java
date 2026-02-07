package com.sma.core.dto.response.resume;

import com.sma.core.enums.CriteriaType;
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
public class EvaluationWeaknessResponse {
    Integer id;
    String weaknessText;
    String suggestion;
    Integer startIndex;
    Integer endIndex;
    String context;
    CriteriaType criterionType;
    Short severity;
}
