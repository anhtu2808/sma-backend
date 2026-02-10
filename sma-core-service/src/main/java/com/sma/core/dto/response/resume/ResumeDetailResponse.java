package com.sma.core.dto.response.resume;

import com.sma.core.enums.ResumeLanguage;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.enums.ResumeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeDetailResponse {
    Integer id;
    Integer candidateId;
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
    ResumeType type;
    Integer rootResumeId;
    String rootResumeName;
    ResumeStatus status;
    ResumeParseStatus parseStatus;
    ResumeLanguage language;
    Boolean isDefault;
    Boolean isDeleted;
    List<ResumeSkillGroupResponse> skillGroups;
    List<ResumeEducationDetailResponse> educations;
    List<ResumeExperienceResponse> experiences;
    List<ResumeProjectResponse> projects;
    List<ResumeCertificationDetailResponse> certifications;
    List<ResumeEvaluationResponse> evaluations;
}
