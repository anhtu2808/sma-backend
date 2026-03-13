package com.sma.core.dto.request.user;

import com.sma.core.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateRecruiterMemberRequest {

    @NotBlank(message = "Full name must not be blank")
    @Size(max = 255, message = "Full name must be less than 255 characters")
    String fullName;

    @NotNull(message = "Gender must not be null")
    Gender gender;
}
