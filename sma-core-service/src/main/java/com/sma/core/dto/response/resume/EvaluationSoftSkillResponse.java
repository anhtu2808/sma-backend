package com.sma.core.dto.response.resume;

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
public class EvaluationSoftSkillResponse {
    Integer id;
    String skillName;
    String evidence;
    Boolean isRequired;
    Boolean isFound;
}
