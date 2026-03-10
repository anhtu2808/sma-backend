# Feature Usage Sequence

## Quota Check And Usage Deduction (Annotated Methods)

```mermaid
sequenceDiagram
    participant Client
    participant Controller as ResumeController
    participant Service as ResumeServiceImpl
    participant Aspect as FeatureQuotaAspect
    participant Runtime as FeatureQuotaRuntimeServiceImpl
    participant SubRepo as SubscriptionRepository
    participant LimitRepo as UsageLimitRepository
    participant EventRepo as UsageEventRepository
    participant StateChecker as CvUploadLimitStateChecker

    Client->>Controller: POST /v1/resumes/upload
    Controller->>Service: uploadResume(request)
    Service->>Aspect: @CheckFeatureQuota (AOP around)
    Aspect->>Runtime: resolveOwnerContext()
    Aspect->>Runtime: findEligibleSubscriptions()
    Aspect->>Runtime: resolveActiveFeature()

    alt usageType == BOOLEAN
        Aspect->>Runtime: hasBooleanEntitlement()
    else usageType == STATE
        Aspect->>Runtime: getStateEffectiveLimit()
        Aspect->>StateChecker: getCurrentUsage()
    else usageType == EVENT
        Aspect->>Runtime: selectEventReservation()
        Runtime->>LimitRepo: findAllByPlanIdInAndFeatureId()
        Runtime->>SubRepo: lockById()
        Runtime->>EventRepo: sumTotal()/sumInPeriod()
    end

    Aspect->>Service: proceed()
    Service-->>Controller: ResumeResponse
    Controller-->>Client: ApiResponse

    opt usageType == EVENT
        Aspect->>Runtime: saveUsageEvent(status=SUCCESS)
        Runtime->>EventRepo: save(UsageEvent + UsageEventContexts)
    end
```

## Current Usage Reporting

```mermaid
sequenceDiagram
    participant Client
    participant Controller as FeatureUsageController
    participant Service as FeatureUsageServiceImpl
    participant Runtime as FeatureQuotaRuntimeServiceImpl
    participant LimitRepo as UsageLimitRepository
    participant EventRepo as UsageEventRepository
    participant Window as SubscriptionQuotaWindowResolver
    participant StateChecker as CvUploadLimitStateChecker

    Client->>Controller: GET /v1/feature-usage
    Controller->>Service: getCurrentUsage()
    Service->>Runtime: resolveOwnerContext()
    Service->>Runtime: findEligibleSubscriptions()
    Service->>LimitRepo: findAllByPlanIdInWithFeature()

    alt usageType == EVENT
        Service->>Window: resolveAnchoredMonthlyWindow()
        Service->>EventRepo: sumTotal()/sumInPeriod() for SUCCESS events
    else usageType == STATE
        Service->>StateChecker: getCurrentUsage()
    else usageType == BOOLEAN
        Service-->>Service: build response without usage numbers
    end

    Service-->>Controller: List<FeatureUsageResponse>
    Controller-->>Client: ApiResponse
```

## Async Refund On Failure

```mermaid
sequenceDiagram
    participant Service as ResumeEvaluationServiceImpl
    participant Quota as QuotaServiceImpl
    participant MQ as Matching Queue
    participant Listener as MatchingResultListener

    Service->>Quota: consumeEventQuota(MATCHING_SCORE, contexts=[JOB, RESUME])
    Quota-->>Service: usageEventId
    Service->>MQ: publish(request + usageEventId)

    alt publish/result processing failed
        Service->>Quota: markUsageEventFailed(usageEventId)
        Listener->>Quota: markUsageEventFailed(usageEventId)
    end
```
