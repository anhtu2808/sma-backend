package com.sma.core.dto.response.candidate;

import com.sma.core.dto.response.resume.ResumeCertificationDetailResponse;
import com.sma.core.dto.response.resume.ResumeEducationDetailResponse;
import com.sma.core.dto.response.resume.ResumeEvaluationResponse;
import com.sma.core.dto.response.resume.ResumeExperienceResponse;
import com.sma.core.dto.response.resume.ResumeProjectResponse;
import com.sma.core.dto.response.resume.ResumeSkillDetailResponse;
import com.sma.core.enums.CandidateShowAs;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.enums.ResumeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateProfileResponse {
    Integer id;
    String jobTitle;
    String linkedinUrl;
    String githubUrl;
    String websiteUrl;
    String address;
    BigDecimal expectedSalaryMin;
    BigDecimal expectedSalaryMax;
    LocalDate availabilityDate;
    Boolean isProfilePublic;
    CandidateShowAs showAs;
    Integer profileCompleteness;

    String fullName;
    String phone;
    String email;
    String avatar;

    Integer profileResumeId;
    String profileResumeName;
    String profileResumeFileName;
    String profileResumeUrl;
    ResumeType resumeType;
    ResumeParseStatus resumeParseStatus;

    List<ResumeSkillDetailResponse> skills;
    List<ResumeEducationDetailResponse> educations;
    List<ResumeExperienceResponse> experiences;
    List<ResumeProjectResponse> projects;
    List<ResumeCertificationDetailResponse> certifications;
}
