package com.sma.core.dto.response.usage;

import com.sma.core.enums.EventSource;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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

    Integer amount;

    EventSource eventSource;

    Integer entityId;

    LocalDateTime createdAt;
}
