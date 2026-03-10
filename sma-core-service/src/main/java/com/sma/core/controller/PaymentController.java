package com.sma.core.controller;

import com.sma.core.dto.request.payment.SePayWebhookRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.enums.PaymentStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<Boolean> confirm(
            @RequestHeader("Authorization") String authorization,
            @RequestBody SePayWebhookRequest request) {
        if (authorization == null || !authorization.startsWith("Apikey "))
            throw new AppException(ErrorCode.MISSING_API_KEY);

        String apiKey = authorization.substring(7);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.confirm(apiKey, request));
    }

    @GetMapping("/{id}/status")
    public ApiResponse<PaymentStatus> getPaymentStatus(@PathVariable Integer id) {
        return ApiResponse.<PaymentStatus>builder()
                .message("Get status of payment successfully")
                .data(paymentService.getPaymentStatus(id))
                .build();
    }

}
