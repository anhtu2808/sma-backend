package com.sma.core.dto.response.feature;

import com.sma.core.enums.UsageType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeatureResponse {
    Integer id;
    String name;
    String description;
    UsageType usageType;
    String featureKey;
    Boolean isActive;
}
