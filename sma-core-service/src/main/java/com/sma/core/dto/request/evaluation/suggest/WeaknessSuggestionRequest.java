package com.sma.core.dto.request.evaluation.suggest;

import com.sma.core.enums.CriteriaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeaknessSuggestionRequest {

    Integer id;
    String weaknessText;
    String context;
    Short severity;
    CriteriaType criterionType;

}
