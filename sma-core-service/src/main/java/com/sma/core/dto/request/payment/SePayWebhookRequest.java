package com.sma.core.dto.request.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SePayWebhookRequest {

    Integer id;
    LocalDateTime transactionDate;
    String content;
    Double transferAmount;

}
