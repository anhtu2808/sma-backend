package com.sma.core.dto.request.feature;

import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.UsageType;
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
public class FeatureRequest {
    @NotBlank(message = "Feature name is required")
    String name;

    String description;

    @NotNull(message = "Usage type is required")
    UsageType usageType;

    @NotNull(message = "Feature key is required")
    FeatureKey featureKey;

    Boolean isActive;
}
