package com.sma.core.dto.request.user;

import com.sma.core.enums.Gender;
import com.sma.core.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateRecruiterMemberRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email is not valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password;

    @NotBlank(message = "Full name must not be blank")
    @Size(max = 255, message = "Full name must be less than 255 characters")
    String fullName;

    @NotNull(message = "Gender must not be null")
    Gender gender;
}
