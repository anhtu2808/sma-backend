package com.sma.core.dto.response.user;

import com.sma.core.enums.Role;
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
public class UserAdminResponse {
    Integer id;
    String fullName;
    String email;
    String avatar;
    Role role;
    UserStatus status;
    String mainActivity;
    String subActivity;
    LocalDateTime joinedAt;
}
