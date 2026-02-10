package com.sma.core.dto.request.planprice;

import com.sma.core.enums.PlanDurationUnit;
import com.sma.core.validation.ValidSalePrice;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidSalePrice
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanPriceRequest {
    @NotNull(message = "Original price is required")
    BigDecimal originalPrice;

    @NotNull(message = "Sale price is required")
    BigDecimal salePrice;

    String currency;

    @NotNull(message = "Duration is required")
    Integer duration;

    @NotNull(message = "Unit is required")
    PlanDurationUnit unit;

    Boolean isActive;
}
