package com.sma.core.dto.model;

import com.sma.core.enums.EventSource;

public record UsageContextModel(
        EventSource eventSource,
        Integer sourceId
) {
}
