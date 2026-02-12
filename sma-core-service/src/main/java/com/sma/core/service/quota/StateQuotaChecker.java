package com.sma.core.service.quota;

public interface StateQuotaChecker {
    long getCurrentUsage(QuotaOwnerContext ownerContext, Object input);
}
