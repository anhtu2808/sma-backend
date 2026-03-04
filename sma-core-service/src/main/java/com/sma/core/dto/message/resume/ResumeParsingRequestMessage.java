package com.sma.core.dto.message.resume;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeParsingRequestMessage {
    Integer resumeId;
    String parseAttemptId;
    String resumeUrl;
    String fileName;
    String resumeName;
    String requestedAt;
}
