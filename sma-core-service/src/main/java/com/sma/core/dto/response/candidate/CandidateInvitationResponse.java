package com.sma.core.dto.response.candidate;

import com.sma.core.dto.response.user.BaseUserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateInvitationResponse {

    Integer id;
    BaseUserResponse user;

}
