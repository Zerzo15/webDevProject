package com.example.webApplication.Dto;

import java.math.BigDecimal;

public record OrderItemDto(
    Long id,
    Long orderId,
    Long productVariantId,
    int quantity,
    BigDecimal unitPrice
) {
}
