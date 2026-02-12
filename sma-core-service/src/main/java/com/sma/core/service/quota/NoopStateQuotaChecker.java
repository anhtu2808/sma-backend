package com.sma.core.service.quota;

public class NoopStateQuotaChecker implements StateQuotaChecker {
    @Override
    public long getCurrentUsage(QuotaOwnerContext ownerContext, Object input) {
        return 0;
    }
}
