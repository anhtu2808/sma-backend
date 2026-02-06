package com.sma.core.dto.message.resume;

import com.fasterxml.jackson.databind.JsonNode;
import com.sma.core.enums.ResumeParseStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeParsingResultMessage {
    Integer resumeId;
    ResumeParseStatus status;
    String errorMessage;
    String processedAt;
    JsonNode parsedData;
}
