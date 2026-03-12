package com.sma.core.service.impl;

import com.sma.core.dto.model.UsageContextModel;
import com.sma.core.entity.*;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.NotificationType;
import com.sma.core.enums.Role;
import com.sma.core.enums.EventSource;
import com.sma.core.enums.UsageEventStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.CandidateRepository;
import com.sma.core.repository.FeatureRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.SubscriptionRepository;
import com.sma.core.repository.UsageEventRepository;
import com.sma.core.repository.UsageLimitRepository;
import com.sma.core.service.FeatureService;
import com.sma.core.service.NotificationService;
import com.sma.core.service.QuotaService;
import com.sma.core.service.SubscriptionService;
import com.sma.core.service.quota.EventUsageCalculator;
import com.sma.core.service.quota.StateQuotaChecker;
import com.sma.core.service.quota.impl.ResumeUploadLimitStateChecker;
import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.utils.JwtTokenProvider;
import org.springframework.context.ApplicationContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuotaServiceImpl implements QuotaService {

    FeatureRepository featureRepository;
    RecruiterRepository recruiterRepository;
    CandidateRepository candidateRepository;
    SubscriptionRepository subscriptionRepository;
    UsageEventRepository usageEventRepository;
    UsageLimitRepository usageLimitRepository;

    FeatureService featureService;
    SubscriptionService subscriptionService;
    EventUsageCalculator eventUsageCalculator;

    ApplicationContext applicationContext;
    NotificationService notificationService;

    static Map<FeatureKey, Integer> RECRUITER_FEATURE_THRESHOLDS = Map.ofEntries(
            Map.entry(FeatureKey.MATCHING_SCORE, 10),
            Map.entry(FeatureKey.DETAIL_MATCHING_SCORE, 10),
            Map.entry(FeatureKey.TALENT_UNLOCK, 5),
            Map.entry(FeatureKey.API_SCORING, 50),
            Map.entry(FeatureKey.JOB_POST_LIMIT, 2),
            Map.entry(FeatureKey.TEAM_MEMBER_LIMIT, 1),
            Map.entry(FeatureKey.CUSTOM_CAREER_PAGE, 1),
            Map.entry(FeatureKey.API_PARSING, 50),
            Map.entry(FeatureKey.TALENT_SEARCH, 10),
            Map.entry(FeatureKey.EXPORT_SHORTLIST, 5),
            Map.entry(FeatureKey.RESUME_PARSING, 20)
    );

    @Override
    public QuotaOwnerContext resolveOwnerContext() {
        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (role == Role.CANDIDATE) {
            Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
            if (!candidateRepository.existsById(candidateId)) {
                throw new AppException(ErrorCode.CANDIDATE_NOT_EXISTED);
            }
            return QuotaOwnerContext.builder().role(role).candidateId(candidateId).build();
        }

        if (role == Role.RECRUITER) {
            Integer recruiterId = JwtTokenProvider.getCurrentRecruiterId();
            Recruiter recruiter = recruiterRepository.findById(recruiterId)
                                                     .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
            return QuotaOwnerContext.builder()
                    .role(role)
                    .recruiterId(recruiterId)
                    .companyId(recruiter.getCompany().getId())
                    .build();
        }

        throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
    }

    @Override
    public boolean hasBooleanEntitlement(List<Subscription> subscriptions, Integer featureId) {
        List<Integer> planIds = extractDistinctPlanIds(subscriptions);
        if (planIds.isEmpty()) {
            return false;
        }
        return usageLimitRepository.existsByPlanIdInAndFeatureId(planIds, featureId);
    }

    @Override
    public long getStateEffectiveLimit(List<Subscription> subscriptions, Integer featureId) {
        List<Integer> planIds = extractDistinctPlanIds(subscriptions);
        if (planIds.isEmpty()) {
            return 0;
        }
        return usageLimitRepository.sumMaxQuotaByPlanIdInAndFeatureId(planIds, featureId);
    }

    @Override
    public EventReservation reserveEventQuota(List<Subscription> subscriptions, Integer featureId, int amount, LocalDateTime now) {
        List<Integer> planIds = extractDistinctPlanIds(subscriptions);
        if (planIds.isEmpty()) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        Map<Integer, UsageLimit> limitsByPlanId = usageLimitRepository.findAllByPlanIdInAndFeatureId(planIds, featureId)
                                                                      .stream()
                                                                      .collect(Collectors.toMap(ul -> ul.getPlan()
                                                                                                        .getId(), Function.identity(), (a, b) -> a));
        if (limitsByPlanId.isEmpty()) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        for (Subscription subscription : subscriptions) {
            Integer planId = subscription.getPlan() != null ? subscription.getPlan().getId() : null;
            if (planId == null) {
                continue;
            }
            UsageLimit usageLimit = limitsByPlanId.get(planId);
            if (usageLimit == null || usageLimit.getMaxQuota() == null) {
                continue;
            }

            Subscription lockedSubscription = subscriptionRepository.lockById(subscription.getId())
                                                                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            Integer lockedPlanId = lockedSubscription.getPlan() != null ? lockedSubscription.getPlan().getId() : null;
            if (lockedPlanId == null) {
                continue;
            }
            if (!lockedPlanId.equals(planId)) {
                usageLimit = limitsByPlanId.get(lockedPlanId);
                if (usageLimit == null || usageLimit.getMaxQuota() == null) {
                    continue;
                }
            }

            // Use EventUsageCalculator to calculate used amount - eliminates duplication
            long usedAmount = eventUsageCalculator.calculate(List.of(lockedSubscription), featureId, usageLimit.getLimitUnit(), null, null);

            if (usedAmount + amount <= usageLimit.getMaxQuota()) {
                return new EventReservation(lockedSubscription.getId(), featureId, amount);
            }
        }

        Feature feature = featureRepository.findById(featureId)
                                           .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));
        throw buildQuotaExceeded(feature);
    }

    @Override
    public UsageEvent commitReservation(EventReservation reservation) {
        return commitReservation(reservation, List.of());
    }

    @Override
    public UsageEvent commitReservation(EventReservation reservation, EventSource entityType, Integer entityId) {
        return entityType == null || entityId == null
                ? commitReservation(reservation, List.of())
                : commitReservation(reservation, List.of(new UsageContextModel(entityType, entityId)));
    }

    @Override
    public UsageEvent commitReservation(EventReservation reservation, List<UsageContextModel> contexts) {
        UsageEvent usageEvent = UsageEvent.builder()
                .subscription(subscriptionRepository.getReferenceById(reservation.subscriptionId()))
                .feature(featureRepository.getReferenceById(reservation.featureId()))
                .amount(reservation.amount())
                .build();

        buildUsageContexts(usageEvent, contexts).forEach(usageEvent::addContext);
        usageEventRepository.save(usageEvent);
        return usageEvent;
    }

    @Override
    public void validateBooleanQuota(FeatureKey featureKey) {
        LocalDateTime now = LocalDateTime.now();
        QuotaOwnerContext ownerContext = resolveOwnerContext();
        List<Subscription> subscriptions = subscriptionService.findEligibleSubscriptions(ownerContext, now);

        Feature feature = featureService.getActiveFeature(featureKey);
        if (!hasBooleanEntitlement(subscriptions, feature.getId())) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }
    }

    @Override
    public void validateStateQuota(FeatureKey featureKey, Object input) {
        LocalDateTime now = LocalDateTime.now();
        QuotaOwnerContext ownerContext = resolveOwnerContext();
        List<Subscription> subscriptions = subscriptionService.findEligibleSubscriptions(ownerContext, now);

        Feature feature = featureService.getActiveFeature(featureKey);
        long limit = getStateEffectiveLimit(subscriptions, feature.getId());

        // Get the state checker from application context
        StateQuotaChecker checker = getStateChecker(featureKey);
        if (checker != null) {
            long currentUsage = checker.getCurrentUsage(ownerContext, input);
            long remainingQuota = limit - currentUsage;

            if (currentUsage >= limit) {
                if (ownerContext.getRole() == Role.CANDIDATE) {
                    notifyCandidateQuota(ownerContext.getCandidateId(), feature, 0, true);
                } else if (ownerContext.getRole() == Role.RECRUITER) {
                    notifyRecruiterQuota(ownerContext.getCompanyId(), feature, 0, true);
                }
                throw new AppException(ErrorCode.FEATURE_QUOTA_EXCEEDED, String.format("Quota exceeded for '%s': limit=%d, current=%d", feature.getName(), limit, currentUsage));
            }
            else {
                if (ownerContext.getRole() == Role.CANDIDATE && remainingQuota == 1) {
                    notifyCandidateQuota(ownerContext.getCandidateId(), feature, remainingQuota, false);
                } else if (ownerContext.getRole() == Role.RECRUITER) {
                    int threshold = RECRUITER_FEATURE_THRESHOLDS.getOrDefault(featureKey, 5);
                    if (remainingQuota > 0 && remainingQuota <= threshold) {
                        notifyRecruiterQuota(ownerContext.getCompanyId(), feature, remainingQuota, false);
                    }
                }
            }
        }
    }

    @Override
    public UsageEvent consumeEventQuota(FeatureKey featureKey, int amount, EventSource entityType, Integer entityId) {
        return entityType == null || entityId == null
                ? consumeEventQuota(featureKey, amount, List.of())
                : consumeEventQuota(featureKey, amount, List.of(new UsageContextModel(entityType, entityId)));
    }

    @Override
    public UsageEvent consumeEventQuota(FeatureKey featureKey, int amount, List<UsageContextModel> contexts) {
        LocalDateTime now = LocalDateTime.now();
        QuotaOwnerContext ownerContext = resolveOwnerContext();
        ensureQuotaAvailable(featureKey, now, ownerContext);
        List<Subscription> subscriptions = subscriptionService.findEligibleSubscriptions(ownerContext, now);

        Feature feature = featureService.getActiveFeature(featureKey);

        EventReservation reservation = reserveEventQuota(subscriptions, feature.getId(), amount, now);

        return commitReservation(reservation, contexts);
    }

    @Override
    @Transactional
    public void markUsageEventFailed(Integer usageEventId) {
        if (usageEventId == null) {
            return;
        }
        usageEventRepository.markStatus(usageEventId, UsageEventStatus.FAIL);
    }

    @Override
    public void checkEventQuotaAvailability(FeatureKey featureKey) {
        LocalDateTime now = LocalDateTime.now();
        QuotaOwnerContext ownerContext = resolveOwnerContext();
        ensureQuotaAvailable(featureKey, now, ownerContext);
    }

    @Override
    public void checkEventQuotaAvailability(FeatureKey featureKey, Role role, Integer ownerContextId) {
        LocalDateTime now = LocalDateTime.now();
        QuotaOwnerContext ownerContext;
        if (role.equals(Role.RECRUITER)) {
            ownerContext = QuotaOwnerContext.builder()
                    .role(role)
                    .companyId(ownerContextId)
                    .build();
        } else {
            ownerContext = QuotaOwnerContext.builder()
                    .role(role)
                    .candidateId(ownerContextId)
                    .build();
        }
        ensureQuotaAvailable(featureKey, now, ownerContext);
    }

    private void ensureQuotaAvailable(FeatureKey featureKey, LocalDateTime now, QuotaOwnerContext ownerContext) {
        List<Subscription> subscriptions = subscriptionService.findEligibleSubscriptions(ownerContext, now);

        if (subscriptions.isEmpty()) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        Feature feature = featureService.getActiveFeature(featureKey);
        List<Integer> planIds = extractDistinctPlanIds(subscriptions);

        UsageLimit usageLimit = usageLimitRepository.findAllByPlanIdInAndFeatureId(planIds, feature.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_INCLUDED));

        if (usageLimit.getMaxQuota() == null) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        long usedAmount = eventUsageCalculator.calculate(
                subscriptions,
                feature.getId(),
                usageLimit.getLimitUnit(),
                null,
                null
        );

        long remainingQuota = usageLimit.getMaxQuota() - usedAmount;
        if (ownerContext.getRole() == Role.RECRUITER) {
            int threshold = RECRUITER_FEATURE_THRESHOLDS.getOrDefault(featureKey, 5);
            if (remainingQuota > 0 && remainingQuota <= threshold) {
                notifyRecruiterQuota(ownerContext.getCompanyId(), feature, remainingQuota, false);
            }
        } else if (ownerContext.getRole() == Role.CANDIDATE) {
            int threshold = 1;
            if (remainingQuota > 0 && remainingQuota <= threshold) {
                notifyCandidateQuota(ownerContext.getCandidateId(), feature, remainingQuota, false);
            }
        }

        if (usedAmount >= usageLimit.getMaxQuota()) {
            if (ownerContext.getRole() == Role.CANDIDATE) {
                notifyCandidateQuota(ownerContext.getCandidateId(), feature, 0, true);
            } else if (ownerContext.getRole() == Role.RECRUITER) {
                notifyRecruiterQuota(ownerContext.getCompanyId(), feature, 0, true); // BỔ SUNG CHO RECRUITER
            }
            throw buildQuotaExceeded(feature);
        }
    }


    private StateQuotaChecker getStateChecker(FeatureKey featureKey) {
        return switch (featureKey) {
            case CV_UPLOAD_LIMIT -> getBean(ResumeUploadLimitStateChecker.class);
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T getBean(Class<T> beanClass) {
        try {
            return applicationContext.getBean(beanClass);
        } catch (Exception e) {
            return null;
        }
    }

    private AppException buildQuotaExceeded(Feature feature) {
        String featureName = feature != null && feature.getName() != null ? feature.getName() : "this feature";

        String message = "Sorry, you have reached the usage limit for " + featureName + ".";
        return new AppException(ErrorCode.FEATURE_QUOTA_EXCEEDED, message);
    }

    private List<Integer> extractDistinctPlanIds(List<Subscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return Collections.emptyList();
        }
        return subscriptions.stream()
                            .map(Subscription::getPlan)
                            .filter(Objects::nonNull)
                            .map(Plan::getId)
                            .distinct()
                            .toList();
    }

    private Set<UsageEventContext> buildUsageContexts(UsageEvent usageEvent, List<UsageContextModel> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return Collections.emptySet();
        }

        return contexts.stream()
                .filter(Objects::nonNull)
                .filter(context -> context.eventSource() != null && context.sourceId() != null)
                .distinct()
                .map(context -> UsageEventContext.builder()
                        .usageEvent(usageEvent)
                        .eventSource(context.eventSource())
                        .sourceId(context.sourceId())
                        .build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void notifyCandidateQuota(Integer candidateId, Feature feature, long remainingQuota, boolean isExceeded) {
        candidateRepository.findById(candidateId).ifPresent(candidate -> {
            if (candidate.getUser() != null) {
                String title = isExceeded ? "Quota Limit Reached" : "Quota Running Low";
                String message = isExceeded
                        ? "You have reached your usage limit for " + feature.getName() + ". Please upgrade your plan to continue using this feature."
                        : "You only have " + remainingQuota + " uses left for " + feature.getName() + ". Consider upgrading your plan to avoid interruption.";

                notificationService.sendCandidateNotification(
                        candidate.getUser(),
                        NotificationType.SYSTEM,
                        title,
                        message,
                        "QUOTA",
                        feature.getId()
                );
            }
        });
    }

    private void notifyRecruiterQuota(Integer companyId, Feature feature, long remainingQuota, boolean isExceeded) {
        String title = isExceeded ? "Quota Limit Reached" : "Quota Running Low";
        String message = isExceeded
                ? "Your company has reached the usage limit for " + feature.getName() + ". Please upgrade your plan to continue using this feature."
                : "Your company has only " + remainingQuota + " uses left for " + feature.getName() + ". Please consider upgrading your plan to avoid interruption.";

        notificationService.sendRecruiterNotification(
                companyId,
                NotificationType.SYSTEM,
                title,
                message,
                "QUOTA",
                feature.getId()
        );
    }
}
