package com.sma.core.aspect;

import com.sma.core.annotation.CheckFeatureQuota;
import com.sma.core.entity.Feature;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.UsageType;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.service.FeatureQuotaRuntimeService;
import com.sma.core.service.quota.impl.NoopStateQuotaChecker;
import com.sma.core.dto.model.QuotaOwnerContext;
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
        CheckFeatureQuota[] quotaChecks = method.getAnnotationsByType(CheckFeatureQuota.class);
        if (quotaChecks.length == 0) {
            return joinPoint.proceed();
        }

        QuotaOwnerContext ownerContext = featureQuotaRuntimeService.resolveOwnerContext();
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> eligibleSubscriptions = featureQuotaRuntimeService.findEligibleSubscriptions(ownerContext, now);
        EvaluationContext spelContext = buildSpelContext(joinPoint, method);

        List<FeatureQuotaRuntimeService.EventReservation> eventReservations = new ArrayList<>();

        for (CheckFeatureQuota check : quotaChecks) {
            Feature feature = featureQuotaRuntimeService.resolveActiveFeature(check.featureKey());
            UsageType usageType = feature.getUsageType();
            if (usageType == null) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            switch (usageType) {
                case BOOLEAN -> assertBooleanEntitlement(eligibleSubscriptions, feature.getId());
                case STATE -> assertStateQuota(check, ownerContext, eligibleSubscriptions, feature, spelContext);
                case EVENT -> eventReservations.add(reserveEventQuota(check, eligibleSubscriptions, feature, spelContext, now));
            }
        }

        Object result = joinPoint.proceed();
        for (FeatureQuotaRuntimeService.EventReservation reservation : eventReservations) {
            featureQuotaRuntimeService.saveUsageEvent(reservation);
        }
        return result;
    }

    private void assertBooleanEntitlement(List<Subscription> subscriptions, Integer featureId) {
        boolean hasEntitlement = featureQuotaRuntimeService.hasBooleanEntitlement(subscriptions, featureId);
        if (!hasEntitlement) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }
    }

    private void assertStateQuota(CheckFeatureQuota check,
                                  QuotaOwnerContext ownerContext,
                                  List<Subscription> subscriptions,
                                  Feature feature,
                                  EvaluationContext spelContext) {
        if (check.stateChecker() == NoopStateQuotaChecker.class) {
            throw new AppException(ErrorCode.STATE_CHECKER_NOT_CONFIGURED);
        }

        long effectiveLimit = featureQuotaRuntimeService.getStateEffectiveLimit(subscriptions, feature.getId());
        if (effectiveLimit <= 0) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        StateQuotaChecker stateQuotaChecker = getStateChecker(check.stateChecker());
        Object stateInput = evaluateStateInput(check, spelContext);
        long currentUsage = stateQuotaChecker.getCurrentUsage(ownerContext, stateInput);
        if (currentUsage >= effectiveLimit) {
            throw buildQuotaExceeded(feature);
        }
    }

    private FeatureQuotaRuntimeService.EventReservation reserveEventQuota(CheckFeatureQuota check,
                                                                          List<Subscription> subscriptions,
                                                                          Feature feature,
                                                                          EvaluationContext spelContext,
                                                                          LocalDateTime now) {
        int amount = evaluateAmount(check.amountExpression(), spelContext);
        if (amount <= 0) {
            throw new AppException(ErrorCode.INVALID_FEATURE_USAGE_AMOUNT);
        }
        try {
            return featureQuotaRuntimeService.selectEventReservation(subscriptions, feature.getId(), amount, now);
        } catch (AppException ex) {
            if (ex.getErrorCode() == ErrorCode.FEATURE_QUOTA_EXCEEDED) {
                throw buildQuotaExceeded(feature);
            }
            throw ex;
        }
    }

    private StateQuotaChecker getStateChecker(Class<? extends StateQuotaChecker> checkerClass) {
        try {
            return applicationContext.getBean(checkerClass);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.STATE_CHECKER_NOT_CONFIGURED);
        }
    }

    private Object evaluateStateInput(CheckFeatureQuota check, EvaluationContext spelContext) {
        if (check.stateInputExpression() == null || check.stateInputExpression().isBlank()) {
            return null;
        }
        return expressionParser.parseExpression(check.stateInputExpression()).getValue(spelContext);
    }

    private int evaluateAmount(String amountExpression, EvaluationContext spelContext) {
        Object rawValue;
        try {
            rawValue = expressionParser.parseExpression(amountExpression).getValue(spelContext);
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

    private AppException buildQuotaExceeded(Feature feature) {
        String featureName = feature != null && feature.getName() != null ? feature.getName() : "này";
        String message = "Xin lỗi, bạn đã hết quota cho tính năng " + featureName;
        return new AppException(ErrorCode.FEATURE_QUOTA_EXCEEDED, message);
    }

    private EvaluationContext buildSpelContext(ProceedingJoinPoint joinPoint, Method method) {
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
