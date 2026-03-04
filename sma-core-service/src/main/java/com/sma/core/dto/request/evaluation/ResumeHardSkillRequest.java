package com.sma.core.dto.request.evaluation;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeHardSkillRequest {

    String name;
    Integer yearsOfExperience;

}
