package com.sma.core.dto.request.usagelimit;

import com.sma.core.enums.UsageLimitUnit;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageLimitRequest {
    @NotNull(message = "Plan id is required")
    Integer planId;

    @NotNull(message = "Feature id is required")
    Integer featureId;

    @NotNull(message = "Max quota is required")
    Integer maxQuota;

    @NotNull(message = "Limit unit is required")
    UsageLimitUnit limitUnit;
}
