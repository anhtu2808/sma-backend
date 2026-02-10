package com.sma.core.dto.response.planprice;

import com.sma.core.enums.PlanDurationUnit;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanPriceResponse {
    Integer id;
    BigDecimal originalPrice;
    BigDecimal salePrice;
    String currency;
    Integer duration;
    PlanDurationUnit unit;
    Boolean isActive;
}
