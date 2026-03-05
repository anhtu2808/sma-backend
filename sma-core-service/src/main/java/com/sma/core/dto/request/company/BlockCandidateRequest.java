package com.sma.core.dto.request.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class BlockCandidateRequest {
    @NotNull(message = "Candidate ID is required")
    Integer candidateId;
    @NotBlank(message = "Reason is required")
    String reason;
}
