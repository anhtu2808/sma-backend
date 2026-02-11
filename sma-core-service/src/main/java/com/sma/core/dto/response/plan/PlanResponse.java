package com.sma.core.dto.response.plan;

import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import com.sma.core.dto.response.planprice.PlanPriceResponse;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanResponse {
    Integer id;
    String name;
    String description;
    String planDetails;
    PlanTarget planTarget;
    PlanType planType;
    String currency;
    Boolean isActive;
    Boolean isPopular;
    List<PlanPriceResponse> planPrices;
}
