package com.sma.core.dto.request.resume;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateResumeExperienceDetailRequest {
    String description;
    String title;
    String position;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isCurrent;
}
