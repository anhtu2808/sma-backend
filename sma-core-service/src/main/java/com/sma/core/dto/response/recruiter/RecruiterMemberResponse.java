package com.sma.core.dto.response.recruiter;

import com.sma.core.enums.Gender;
import com.sma.core.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RecruiterMemberResponse {
    Integer id;
    Integer userId;
    String fullName;
    String email;
    String avatar;
    Gender gender;
    UserStatus status;
    Boolean isRootRecruiter;
    Boolean isVerified;
    LocalDateTime lastLoginAt;
}
