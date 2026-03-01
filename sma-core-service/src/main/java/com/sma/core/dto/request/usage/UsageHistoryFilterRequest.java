package com.sma.core.dto.request.usage;

import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.EventSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UsageHistoryFilterRequest {

    FeatureKey featureKey;

    @Schema(description = "Filter from date")
    LocalDateTime startDate;

    @Schema(description = "Filter to date")
    LocalDateTime endDate;

    @Schema(description = "Filter by context entity type (JOB, APPLICATION, RESUME, EXPORT_RECORD)")
    EventSource eventSource;

    @Schema(description = "Filter by specific context entity ID")
    Integer sourceId;

    @Builder.Default
    @Schema(description = "Page number (default: 0)")
    Integer page = 0;

    @Builder.Default
    @Schema(description = "Page size (default: 10)")
    Integer size = 10;
}
