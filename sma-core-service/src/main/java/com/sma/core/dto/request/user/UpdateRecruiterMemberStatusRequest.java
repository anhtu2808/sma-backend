package com.sma.core.dto.request.user;

import com.sma.core.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateRecruiterMemberStatusRequest {

    @NotNull(message = "Status must not be null")
    UserStatus status;
}
