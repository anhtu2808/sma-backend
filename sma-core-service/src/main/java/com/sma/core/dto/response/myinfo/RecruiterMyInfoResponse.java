package com.sma.core.dto.response.myinfo;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RecruiterMyInfoResponse {
    Integer id;
    Integer companyId;
    Boolean isVerified;
    Boolean isRootRecruiter;
    LocalDateTime verifiedAt;
    UserMyInfoResponse user;
}
