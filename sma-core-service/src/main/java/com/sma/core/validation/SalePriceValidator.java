package com.sma.core.validation;

import com.sma.core.dto.request.planprice.PlanPriceRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class SalePriceValidator implements ConstraintValidator<ValidSalePrice, PlanPriceRequest> {
    @Override
    public boolean isValid(PlanPriceRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        BigDecimal original = value.getOriginalPrice();
        BigDecimal sale = value.getSalePrice();
        if (original == null || sale == null) {
            return true;
        }
        return sale.compareTo(original) <= 0;
    }
}
