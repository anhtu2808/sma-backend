package com.sma.core.dto.response.myinfo;

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
public class UserMyInfoResponse {
    Integer id;
    String email;
    UserStatus status;
    String fullName;
    String avatar;
    LocalDateTime lastLoginAt;
    LocalDate dateOfBirth;
    Gender gender;
    Role role;
}
