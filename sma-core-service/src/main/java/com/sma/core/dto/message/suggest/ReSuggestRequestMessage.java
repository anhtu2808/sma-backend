package com.sma.core.dto.message.suggest;

import com.sma.core.dto.request.evaluation.suggest.WeaknessSuggestionRequest;
import com.sma.core.enums.JobLevel;
import com.sma.core.enums.MatchLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReSuggestRequestMessage {

    Integer suggestionId;
    String jobTitle;
    JobLevel jobLevel;
    String summary;
    String weakness;
    String scoringCriteriaContext;
    String rule;
    String label;
    String context;
    String suggestion;
    String aiExplanation;
    String description;

}
