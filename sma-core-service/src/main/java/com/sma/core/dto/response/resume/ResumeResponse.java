package com.sma.core.dto.response.resume;

import com.sma.core.enums.ResumeLanguage;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.enums.ResumeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeResponse {

     Integer id;
    
     String fileName;
    
     String originalFile;
    
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

     ResumeType type;

     Integer rootResumeId;

     ResumeStatus status;

     ResumeParseStatus parseStatus;

     ResumeLanguage language;

     Boolean isDefault;

     Boolean isOverrided;
}
