package com.sma.core.dto.response.recruiter;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RecruiterShortResponse {
    Integer id;
    String avatar;
    String fullName;
    String email;
    Boolean isRootCandidate;
    Boolean isVerified;
}
