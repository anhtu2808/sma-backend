package com.sma.core.dto.message.resume.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResumeExperienceDetail {
    String description;
    String title;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isCurrent;
    Integer orderIndex;

    @Builder.Default
    List<ParsedExperienceSkill> skills = new ArrayList<>();
}
