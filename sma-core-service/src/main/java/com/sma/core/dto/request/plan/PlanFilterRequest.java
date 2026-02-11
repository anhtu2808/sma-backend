package com.sma.core.dto.request.plan;

import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanFilterRequest {
    String name;
    PlanTarget planTarget;
    PlanType planType;
    Boolean isActive;
    Boolean isPopular;

    @Builder.Default
    Integer page = 0;

    @Builder.Default
    Integer size = 10;
}
