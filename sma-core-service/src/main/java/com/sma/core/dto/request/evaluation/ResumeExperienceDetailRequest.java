package com.sma.core.dto.request.evaluation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeExperienceDetailRequest {

    String description;
    String title;
    Set<ResumeExperienceSkillRequest> skills;
}
