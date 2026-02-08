package com.sma.core.dto.response.resume;

import com.sma.core.enums.GapType;
import com.sma.core.enums.ImpactType;
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
public class EvaluationGapResponse {
    Integer id;
    GapType gapType;
    String itemName;
    String description;
    ImpactType impact;
    Float impactScore;
    String suggestion;
}
