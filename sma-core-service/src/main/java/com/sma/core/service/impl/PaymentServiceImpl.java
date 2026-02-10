package com.sma.core.service.impl;

import com.sma.core.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Service;

@Service
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    @Override
    public String createQR() {
        return "";
    }
}
