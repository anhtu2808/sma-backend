package com.sma.core.dto.message.matching;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaScoreData {

    String criteriaName;
    Float aiScore;
    String aiExplanation;
    List<CriteriaScoreDetailData> details;

}
