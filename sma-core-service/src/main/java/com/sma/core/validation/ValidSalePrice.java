package com.sma.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SalePriceValidator.class)
@Documented
public @interface ValidSalePrice {
    String message() default "Sale price must be less than or equal to original price";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
