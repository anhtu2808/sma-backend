package com.sma.core.dto.response.user;

import com.sma.core.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseUserResponse {

    Integer id;
    String fullName;
    String email;
    String avatar;
    LocalDate dateOfBirth;
    Gender gender;

}
