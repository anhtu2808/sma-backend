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

    Integer jobId;
    Integer resumeId;
    Integer evaluationId;
    String jobName;
    JobLevel jobLevel;
    Boolean isTrueLevel;
    Boolean hasRelatedExperience;
    MatchLevel matchLevel;
    String summary;
    WeaknessSuggestionRequest weakness;

}
