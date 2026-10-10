package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.webApplication.Entity.PaymentMethod;
import com.example.webApplication.Entity.PaymentStatus;

public record PaymentDto(
    Long id,
    OrderDto order,
    PaymentMethod payment_method,
    PaymentStatus payment_status,
    BigDecimal amount,
    String transaction_id,
    Instant created_at,
    Instant updated_at
) {

}
