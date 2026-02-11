package com.sma.core.dto.request.plan;

import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanUpdateRequest {
    @NotBlank(message = "Plan name is required")
    String name;

    String description;
    String planDetails;

    @NotNull(message = "Plan target is required")
    PlanTarget planTarget;

    @NotNull(message = "Plan type is required")
    PlanType planType;

    String currency;
    Boolean isPopular;
}
