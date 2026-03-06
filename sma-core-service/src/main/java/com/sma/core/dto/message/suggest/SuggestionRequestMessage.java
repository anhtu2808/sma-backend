package com.sma.core.dto.message.suggest;

import com.sma.core.dto.request.evaluation.suggest.GapSuggestionRequest;
import com.sma.core.dto.request.evaluation.suggest.WeaknessSuggestionRequest;
import com.sma.core.enums.JobLevel;
import com.sma.core.enums.MatchLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestionRequestMessage {

    Integer jobId;
    Integer resumeId;
    Integer evaluationId;
    String jobName;
    JobLevel jobLevel;
    Set<GapSuggestionRequest> gaps;
    Set<WeaknessSuggestionRequest> weaknesses;
    Boolean isTrueLevel;
    Boolean hasRelatedExperience;
    MatchLevel matchLevel;
    String summary;

}
