package com.sma.core.dto.request.evaluation.suggest;

import com.sma.core.enums.GapType;
import com.sma.core.enums.ImpactType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GapSuggestionRequest {

    Integer id;
    GapType gapType;
    String itemName;
    String description;
    ImpactType impact;

}
