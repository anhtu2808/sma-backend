package com.sma.core.dto.response.user;

import com.sma.core.enums.Gender;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserDetailResponse {
    UserAdminResponse baseInfo;
    Gender gender;
    LocalDate dateOfBirth;
    RecruiterDetailResponse recruiterDetail;
    CandidateDetailResponse candidateDetail;
}
