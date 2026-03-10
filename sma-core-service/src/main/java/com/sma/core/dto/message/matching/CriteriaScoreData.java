package com.sma.core.dto.message.matching;

import com.sma.core.enums.CriteriaType;
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

    CriteriaType criteriaType;
    Float aiScore;
    String aiExplanation;
    List<CriteriaScoreDetailData> details;

}
