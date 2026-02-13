package com.sma.core.service.quota.impl;

import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.service.quota.StateQuotaChecker;

public class NoopStateQuotaChecker implements StateQuotaChecker {
    @Override
    public long getCurrentUsage(QuotaOwnerContext ownerContext, Object input) {
        return 0;
    }
}
