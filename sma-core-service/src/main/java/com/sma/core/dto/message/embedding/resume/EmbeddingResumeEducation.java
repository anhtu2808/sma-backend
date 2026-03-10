package com.sma.core.dto.message.embedding.resume;

import com.sma.core.enums.DegreeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmbeddingResumeEducation {

    String institution;
    DegreeType degree;
    String majorField;

}
