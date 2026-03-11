package com.sma.core.dto.message.resume;

import com.sma.core.dto.message.resume.parsed.ParsedResumePayload;
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
    Integer usageEventId;
    String parseAttemptId;
    ResumeParseStatus status;
    String errorMessage;
    String processedAt;
    ParsedResumePayload parsedData;
}
