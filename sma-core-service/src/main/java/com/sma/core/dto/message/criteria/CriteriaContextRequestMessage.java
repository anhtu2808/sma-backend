package com.sma.core.dto.message.criteria;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaContextRequestMessage {

    Integer jobId;
    String jobName;
    String about;
    String responsibilities;
    String requirement;
    String jobLevel;
    Integer experienceTime;
    String workingModel;
    List<String> skills;
    List<String> domains;
    List<String> criteriaTypes;

    /**
     * Map of criteriaType -> scoringCriteriaId for linking results back.
     */
    Map<String, Integer> criteriaTypeToScoringCriteriaId;
}
