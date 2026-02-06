package com.sma.core.dto.response.user;

import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserDetailResponse {
    Integer id;
    String email;
    String fullName;
    String avatar;
    UserStatus status;
    Role role;
//
//    CandidateDetailDTO candidate;
//    RecruiterDetailDTO recruiter;
}
