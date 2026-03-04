package com.sma.core.dto.message.resume.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResumeCertification {
    String name;
    String issuer;
    String credentialUrl;
    String image;
    String description;
}
