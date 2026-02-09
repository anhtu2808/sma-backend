package com.sma.core.dto.response.resume;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeExperienceDetailResponse {
    Integer id;
    String description;
    String title;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isCurrent;
    Integer orderIndex;
    List<ExperienceSkillResponse> skills;
}
