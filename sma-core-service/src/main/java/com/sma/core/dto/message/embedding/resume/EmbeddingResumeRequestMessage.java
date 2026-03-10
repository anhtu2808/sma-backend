package com.sma.core.dto.message.embedding.resume;

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
    String address;
    Set<EmbeddingResumeSkill> skills;
    Set<EmbeddingResumeEducation> educations;
    Set<EmbeddingResumeExperience> experiences;
    Set<EmbeddingResumeProject> projects;
}
