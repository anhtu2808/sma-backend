package com.sma.core.dto.response.company;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlockedCandidateResponse {

    Integer candidateId;
    String fullName;
    String email;
    String reason;
    LocalDateTime blockDate;
    String createdBy;
    Integer blockedById;
}
