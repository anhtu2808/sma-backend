package com.sma.core.dto.response.subscription;

import com.sma.core.dto.response.payment.CreatePaymentResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateSubscriptionResponse {

    Integer id;
    CreatePaymentResponse payment;

}
