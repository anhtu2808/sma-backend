package com.sma.core.dto.response.job;

import com.sma.core.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProposedCVResponse {

    Integer resumeId;
    Integer candidateId;
    Integer jobId;
    String fullName;
    String jobTitle;
    String address;
    Gender gender;
    Float matchRate;

}
