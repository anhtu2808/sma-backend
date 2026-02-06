package com.sma.core.dto.message.resume;

import com.fasterxml.jackson.databind.JsonNode;
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
    String status;
    String errorMessage;
    String processedAt;
    JsonNode parsedData;

    public boolean isSuccess() {
        return status != null && "SUCCESS".equalsIgnoreCase(status.trim());
    }
}
