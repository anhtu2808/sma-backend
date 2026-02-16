package com.sma.core.annotation;

import com.sma.core.enums.FeatureKey;
import com.sma.core.service.quota.impl.NoopStateQuotaChecker;
import com.sma.core.service.quota.StateQuotaChecker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Repeatable(CheckFeatureQuotas.class)
public @interface CheckFeatureQuota {
    FeatureKey featureKey();

    String amountExpression() default "1";

    Class<? extends StateQuotaChecker> stateChecker() default NoopStateQuotaChecker.class;

    String stateInputExpression() default "";
}
