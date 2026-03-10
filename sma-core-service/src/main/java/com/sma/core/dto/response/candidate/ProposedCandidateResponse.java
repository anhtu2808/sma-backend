package com.sma.core.dto.response.candidate;

import com.sma.core.dto.response.user.BaseUserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ProposedCandidateResponse {

    Integer id;
    String jobTitle;
    Float matchRate;
    BaseUserResponse user;
}
