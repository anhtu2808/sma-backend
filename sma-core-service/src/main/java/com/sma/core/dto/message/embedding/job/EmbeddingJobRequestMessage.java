package com.sma.core.dto.message.embedding.job;

import com.sma.core.entity.JobExpertise;
import com.sma.core.enums.JobLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmbeddingJobRequestMessage {
    Integer id;
    String title;
    String about;
    String responsibilities;
    String requirement;
    JobLevel jobLevel;
    String expertiseName;
    Set<EmbeddingJobSkill> skills;
}
