package com.sma.core.service.quota;

import com.sma.core.dto.model.QuotaOwnerContext;

public interface StateQuotaChecker {
    long getCurrentUsage(QuotaOwnerContext ownerContext, Object input);
}
