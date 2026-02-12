package com.sma.core.aspect;

import com.sma.core.annotation.CheckFeatureQuota;
import com.sma.core.entity.Feature;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.UsageType;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.service.FeatureQuotaRuntimeService;
import com.sma.core.service.quota.NoopStateQuotaChecker;
import com.sma.core.service.quota.QuotaOwnerContext;
import com.sma.core.service.quota.StateQuotaChecker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeatureQuotaAspect {

    FeatureQuotaRuntimeService featureQuotaRuntimeService;
    ApplicationContext applicationContext;

    ExpressionParser expressionParser = new SpelExpressionParser();
    ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(com.sma.core.annotation.CheckFeatureQuota) || @annotation(com.sma.core.annotation.CheckFeatureQuotas)")
    @Transactional
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), joinPoint.getTarget().getClass());
        CheckFeatureQuota[] checks = method.getAnnotationsByType(CheckFeatureQuota.class);
        if (checks.length == 0) {
            return joinPoint.proceed();
        }

        QuotaOwnerContext ownerContext = featureQuotaRuntimeService.resolveOwnerContext();
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> eligibleSubscriptions = featureQuotaRuntimeService.findEligibleSubscriptions(ownerContext, now);
        EvaluationContext evaluationContext = buildEvaluationContext(joinPoint, method);

        List<FeatureQuotaRuntimeService.EventReservation> pendingReservations = new ArrayList<>();

        for (CheckFeatureQuota check : checks) {
            Feature feature = featureQuotaRuntimeService.resolveActiveFeature(check.featureKey());
            UsageType usageType = feature.getUsageType();
            if (usageType == null) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            switch (usageType) {
                case BOOLEAN -> validateBooleanFeature(eligibleSubscriptions, feature.getId());
                case STATE -> validateStateFeature(check, ownerContext, eligibleSubscriptions, feature, evaluationContext);
                case EVENT -> pendingReservations.add(validateEventFeature(check, eligibleSubscriptions, feature, evaluationContext, now));
            }
        }

        Object result = joinPoint.proceed();
        for (FeatureQuotaRuntimeService.EventReservation reservation : pendingReservations) {
            featureQuotaRuntimeService.saveUsageEvent(reservation);
        }
        return result;
    }

    private void validateBooleanFeature(List<Subscription> subscriptions, Integer featureId) {
        boolean hasEntitlement = featureQuotaRuntimeService.hasBooleanEntitlement(subscriptions, featureId);
        if (!hasEntitlement) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }
    }

    private void validateStateFeature(CheckFeatureQuota check,
                                      QuotaOwnerContext ownerContext,
                                      List<Subscription> subscriptions,
                                      Feature feature,
                                      EvaluationContext evaluationContext) {
        if (check.stateChecker() == NoopStateQuotaChecker.class) {
            throw new AppException(ErrorCode.STATE_CHECKER_NOT_CONFIGURED);
        }

        long effectiveLimit = featureQuotaRuntimeService.getStateEffectiveLimit(subscriptions, feature.getId());
        if (effectiveLimit <= 0) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        StateQuotaChecker stateQuotaChecker = resolveStateChecker(check.stateChecker());
        Object stateInput = resolveStateInput(check, evaluationContext);
        long currentUsage = stateQuotaChecker.getCurrentUsage(ownerContext, stateInput);
        if (currentUsage >= effectiveLimit) {
            throw new AppException(ErrorCode.FEATURE_QUOTA_EXCEEDED);
        }
    }

    private FeatureQuotaRuntimeService.EventReservation validateEventFeature(CheckFeatureQuota check,
                                                                             List<Subscription> subscriptions,
                                                                             Feature feature,
                                                                             EvaluationContext evaluationContext,
                                                                             LocalDateTime now) {
        int amount = resolveAmount(check.amountExpression(), evaluationContext);
        if (amount <= 0) {
            throw new AppException(ErrorCode.INVALID_FEATURE_USAGE_AMOUNT);
        }
        return featureQuotaRuntimeService.selectEventReservation(subscriptions, feature.getId(), amount, now);
    }

    private StateQuotaChecker resolveStateChecker(Class<? extends StateQuotaChecker> checkerClass) {
        try {
            return applicationContext.getBean(checkerClass);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.STATE_CHECKER_NOT_CONFIGURED);
        }
    }

    private Object resolveStateInput(CheckFeatureQuota check, EvaluationContext evaluationContext) {
        if (check.stateInputExpression() == null || check.stateInputExpression().isBlank()) {
            return null;
        }
        return expressionParser.parseExpression(check.stateInputExpression()).getValue(evaluationContext);
    }

    private int resolveAmount(String amountExpression, EvaluationContext evaluationContext) {
        Object rawValue;
        try {
            rawValue = expressionParser.parseExpression(amountExpression).getValue(evaluationContext);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.INVALID_FEATURE_USAGE_AMOUNT);
        }

        if (rawValue == null) {
            throw new AppException(ErrorCode.INVALID_FEATURE_USAGE_AMOUNT);
        }
        if (rawValue instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(rawValue.toString().trim());
        } catch (NumberFormatException ex) {
            throw new AppException(ErrorCode.INVALID_FEATURE_USAGE_AMOUNT);
        }
    }

    private EvaluationContext buildEvaluationContext(ProceedingJoinPoint joinPoint, Method method) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
        }
        return context;
    }
}
