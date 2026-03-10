package com.sma.core.dto.response.usage;

import com.sma.core.enums.UsageEventStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UsageEventResponse {

    Integer id;

    String featureKey;

    String featureName;

    String planName;

    Integer amount;

    UsageEventStatus status;

    List<UsageEventContextResponse> contexts;

    LocalDateTime createdAt;
}
