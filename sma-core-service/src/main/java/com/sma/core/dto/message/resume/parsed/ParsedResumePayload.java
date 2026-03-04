package com.sma.core.dto.message.resume.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResumePayload {
    ParsedResume resume;

    @Builder.Default
    List<ParsedResumeSkillGroup> resumeSkills = new ArrayList<>();

    @Builder.Default
    List<ParsedResumeEducation> resumeEducations = new ArrayList<>();

    @Builder.Default
    List<ParsedResumeExperience> resumeExperiences = new ArrayList<>();

    @Builder.Default
    List<ParsedResumeProject> resumeProjects = new ArrayList<>();

    @Builder.Default
    List<ParsedResumeCertification> resumeCertifications = new ArrayList<>();

    ParsedMetadata metadata;
}
