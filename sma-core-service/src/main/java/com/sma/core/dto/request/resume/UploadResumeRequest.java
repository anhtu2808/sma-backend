package com.sma.core.dto.request.resume;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UploadResumeRequest {
    String resumeName;

    String fileName;

    String resumeUrl;
}
