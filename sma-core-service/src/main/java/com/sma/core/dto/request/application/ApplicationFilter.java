package com.sma.core.dto.request.application;

import com.sma.core.enums.ApplicationStatus;
import com.sma.core.enums.MatchLevel;
import com.sma.core.enums.ResumeLanguage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationFilter {
    Integer jobId;
    ApplicationStatus status;
    MatchLevel matchLevel;
    Float minScore;
    String keyword;
    String location;
    List<String> skills;
    ResumeLanguage language;
    int page = 0;
    int size = 10;
}
