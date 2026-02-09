package com.sma.core.dto.request.resume;

import com.sma.core.enums.ResumeLanguage;
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
public class UpdateResumeRequest {
    String resumeName;
    String fileName;
    String resumeUrl;
    String addressInResume;
    String phoneInResume;
    String emailInResume;
    String githubLink;
    String linkedinLink;
    String portfolioLink;
    String fullName;
    String avatar;
    ResumeLanguage language;
    Boolean isDefault;
}
