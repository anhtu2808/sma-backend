package com.sma.core.dto.message.embedding.resume;

import com.sma.core.enums.ResumeLanguage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmbeddingResumeRequestMessage {

    Integer id;
    String jobTitle;
    ResumeLanguage language;
    String location;
    Set<EmbeddingResumeSkill> skills;
    Set<EmbeddingResumeEducation> educations;
    Set<EmbeddingResumeExperience> experiences;
    Set<EmbeddingResumeProject> projects;
}
