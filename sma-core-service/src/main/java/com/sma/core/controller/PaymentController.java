package com.sma.core.controller;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    PaymentService paymentService;

    @PostMapping
    public ApiResponse<String> createQR(){
        return ApiResponse.<String>builder()
                .message("Create QR successfully")
                .data(paymentService.createQR())
                .build();
    }

}
