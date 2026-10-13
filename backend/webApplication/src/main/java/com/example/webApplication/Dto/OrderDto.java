package com.example.webApplication.Dto;

import java.math.BigDecimal;

import com.example.webApplication.Entity.OrderStatus;

public record OrderDto(
    Long id,
    Long userId,
    BigDecimal totalAmount,
    OrderStatus status,
    String shippingAddressText
) {
}
