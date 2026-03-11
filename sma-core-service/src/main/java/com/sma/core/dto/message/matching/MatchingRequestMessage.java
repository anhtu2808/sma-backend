package com.sma.core.dto.message.matching;

import com.sma.core.dto.request.evaluation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingRequestMessage {

    Integer evaluationId;
    Integer usageEventId;
    Integer resumeId;
    Integer jobId;
    String resumeName;
    String resumeFileName;
    String candidateFullName;
    String jobTitle;
    Set<JobScoringCriteriaRequest> criteria;
    String rawResumeText;
    String matchingType; // "OVERVIEW" or "DETAIL"
    Map<String, Object> overviewScores; // Overview data for detail supplement mode

}
