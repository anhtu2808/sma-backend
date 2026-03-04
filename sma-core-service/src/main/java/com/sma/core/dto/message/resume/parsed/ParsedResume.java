package com.sma.core.dto.message.resume.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sma.core.enums.ResumeLanguage;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResume {
    String resumeName;
    String fileName;
    String rawText;
    String addressInResume;
    String phoneInResume;
    String emailInResume;
    String githubLink;
    String linkedinLink;
    String portfolioLink;
    String fullName;
    String avatar;
    String resumeUrl;
    ResumeLanguage language;
    Boolean isDefault;
}
