package com.sma.core.dto.request.auth;

import com.sma.core.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RegisterRequest {

    String email;
    String password;
    Gender gender;
    String fullName;

}
